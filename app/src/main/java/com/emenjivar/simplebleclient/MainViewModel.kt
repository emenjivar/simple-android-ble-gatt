package com.emenjivar.simplebleclient

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emenjivar.simplebleclient.ble.BleConnectionState
import com.emenjivar.simplebleclient.ble.BleNotifications
import com.emenjivar.simplebleclient.ble.CustomBluetoothManager
import com.emenjivar.simplebleclient.ble.commands.GetIPAddress
import com.emenjivar.simplebleclient.ble.commands.LEDCommand
import com.emenjivar.simplebleclient.ble.commands.ReadLedStatus
import com.emenjivar.simplebleclient.ble.commands.WriteLedStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val customBluetoothManager: CustomBluetoothManager,
    bleNotifications: BleNotifications
) : ViewModel() {
    val pairedDevices = customBluetoothManager.scannedDevices
    val connectionState = customBluetoothManager.connectionState

    val ledState = bleNotifications.observe(ReadLedStatus)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LEDCommand.OFF
        )

    val ipAddress = bleNotifications.observe(GetIPAddress)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = "0.0.0.0"
        )

    init {
        connectionState.onEach { state ->
            if (state is BleConnectionState.Connected && state.ready) {
                customBluetoothManager.readCharacteristic(GetIPAddress)
            }
        }.launchIn(viewModelScope)
    }

    fun startScan() = customBluetoothManager.startScan()
    fun stopScan() = customBluetoothManager.stopScan()
    fun connect(device: BluetoothDevice) = customBluetoothManager.connect(device)
    fun disconnect() = customBluetoothManager.disconnect()

    fun updateLedState(state: LEDCommand) {
        customBluetoothManager.writeCharacteristic(WriteLedStatus, state)
    }
}