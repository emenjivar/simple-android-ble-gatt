package com.emenjivar.simplebleclient.ui.main

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emenjivar.simplebleclient.ble.BleConnectionState
import com.emenjivar.simplebleclient.ble.BleNotifications
import com.emenjivar.simplebleclient.ble.CustomBluetoothManager
import com.emenjivar.simplebleclient.ble.commands.GetIPAddress
import com.emenjivar.simplebleclient.ble.commands.GetSSID
import com.emenjivar.simplebleclient.ble.commands.LEDCommand
import com.emenjivar.simplebleclient.ble.commands.ReadLedStatus
import com.emenjivar.simplebleclient.ble.commands.WriteLedStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val customBluetoothManager: CustomBluetoothManager
) : ViewModel() {
    val uiState = MutableStateFlow(MainUiState())

    val pairedDevices = customBluetoothManager.scannedDevices
    val connectionState = customBluetoothManager.connectionState

    init {
        combine(pairedDevices, connectionState) { pairedDevices, connectionState ->
            uiState.update {
                it.copy(pairedDevices = pairedDevices, connectionState = connectionState)
            }
        }.launchIn(viewModelScope)

//        connectionState.onEach { state ->
//            if (state is BleConnectionState.Connected && state.ready) {
//                customBluetoothManager.readCharacteristic(GetIPAddress)
//                customBluetoothManager.readCharacteristic(GetSSID)
//            }
//        }.launchIn(viewModelScope)
    }

    fun startScan() = customBluetoothManager.startScan()
    fun stopScan() = customBluetoothManager.stopScan()
    fun connect(device: BluetoothDevice) = customBluetoothManager.connect(device)
    fun disconnect() = customBluetoothManager.disconnect()
}