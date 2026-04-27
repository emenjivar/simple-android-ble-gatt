package com.emenjivar.simplebleclient.ui.detail

import com.emenjivar.simplebleclient.ble.BleConnectionState
import com.emenjivar.simplebleclient.ble.commands.LEDCommand

data class DetailUiState(
    val macAddress: String = "N/A",
    val deviceName: String = "",
    val ssid: String = "N/A",
    val ipAddress: String = "N/A",
    val ledState: LEDCommand = LEDCommand.OFF,
    val connectionState: BleConnectionState = BleConnectionState.Disconnected
)
