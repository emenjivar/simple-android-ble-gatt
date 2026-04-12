package com.emenjivar.simplebleclient.ble.commands

import com.emenjivar.simplebleclient.ble.BleCommand
import com.emenjivar.simplebleclient.ble.getSSIDCharacteristicUUID
import com.emenjivar.simplebleclient.ble.primaryServiceUUID

object GetSSID : BleCommand.Read<String>(
    service = primaryServiceUUID,
    characteristic = getSSIDCharacteristicUUID
) {
    override fun decode(bytes: ByteArray): String {
        return String(bytes, Charsets.UTF_8)
    }
}
