package com.emenjivar.simplebleclient.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emenjivar.simplebleclient.ble.BleConnectionState
import com.emenjivar.simplebleclient.ble.BleNotifications
import com.emenjivar.simplebleclient.ble.CustomBluetoothManager
import com.emenjivar.simplebleclient.ble.commands.GetIPAddress
import com.emenjivar.simplebleclient.ble.commands.GetSSID
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

@HiltViewModel(assistedFactory = DetailViewModel.Factory::class)
class DetailViewModel @AssistedInject constructor(
    private val customBluetoothManager: CustomBluetoothManager,
    @Assisted private val route: DetailRoute,
    bleNotifications: BleNotifications,
) : ViewModel() {

    // TODO: use backing fields here
    private val _uiState = MutableStateFlow(DetailUiState(macAddress = route.macAddress))
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    // Assuming a connected device
    val connectionState = customBluetoothManager.connectionState

    private val ipAddress = bleNotifications.observe(GetIPAddress)
    private val ssid = bleNotifications.observe(GetSSID)

    init {
        // Read characteristics when connection is ready
        connectionState.onEach { state ->
            if (state is BleConnectionState.Connected && state.ready) {
                customBluetoothManager.readCharacteristic(GetIPAddress)
                customBluetoothManager.readCharacteristic(GetSSID)
            }
        }.launchIn(viewModelScope)

        // Listed BLE responses and updated uiState
        combine(ipAddress, ssid) { ipAddress, ssid ->
            _uiState.update {
                it.copy(
                    ipAddress = ipAddress,
                    ssid = ssid
                )
            }
        }.launchIn(viewModelScope)
    }

    @AssistedFactory
    interface Factory {
        fun create(route: DetailRoute): DetailViewModel
    }
}