package com.emenjivar.simplebleclient.ui.main

import android.bluetooth.BluetoothDevice
import com.emenjivar.simplebleclient.ble.BleConnectionState

data class MainUiState(
    val pairedDevices: List<BluetoothDevice> = emptyList<BluetoothDevice>(),
    val connectionState: BleConnectionState = BleConnectionState.Disconnected
)

