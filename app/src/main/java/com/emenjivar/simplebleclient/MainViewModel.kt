package com.emenjivar.simplebleclient

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emenjivar.simplebleclient.ble.BleNotifications
import com.emenjivar.simplebleclient.ble.CustomBluetoothManager
import com.emenjivar.simplebleclient.ble.commands.LEDCommand
import com.emenjivar.simplebleclient.ble.commands.ReadLedStatus
import com.emenjivar.simplebleclient.ble.commands.WriteLedStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val customBluetoothManager: CustomBluetoothManager,
    bleNotifications: BleNotifications
) : ViewModel() {
    val pairedDevices = customBluetoothManager.pairedDevices
    val connectedDevice = customBluetoothManager.connectedDevice
    val isConnecting = customBluetoothManager.isConnecting
    val ledState = bleNotifications.observe(ReadLedStatus)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LEDCommand.OFF
        )

    fun startScan() = customBluetoothManager.startScan()
    fun stopScan() = customBluetoothManager.stopScan()
    fun connect(device: BluetoothDevice) = customBluetoothManager.connect(device)
    fun disconnect() = customBluetoothManager.disconnect()

    fun updateLedState(state: LEDCommand) {
        customBluetoothManager.writeCharacteristic(WriteLedStatus, state)
    }
}