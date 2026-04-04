package com.emenjivar.simplebleclient.ble

import android.bluetooth.BluetoothDevice

sealed class BleConnectionState {
    object Disconnected: BleConnectionState()
    object Connecting: BleConnectionState()
    data class Connected(val device: BluetoothDevice) : BleConnectionState()
    object Disconnecting : BleConnectionState()
    object Failed : BleConnectionState()

    fun isConnected() = this is Connected
}
