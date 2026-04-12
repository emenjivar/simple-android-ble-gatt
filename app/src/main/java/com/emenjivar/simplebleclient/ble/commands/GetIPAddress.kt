package com.emenjivar.simplebleclient.ble.commands

import com.emenjivar.simplebleclient.ble.BleCommand
import com.emenjivar.simplebleclient.ble.getIPCharacteristicUUID
import com.emenjivar.simplebleclient.ble.primaryServiceUUID

object GetIPAddress : BleCommand.Read<String>(
    service = primaryServiceUUID,
    characteristic = getIPCharacteristicUUID
) {
    override fun decode(bytes: ByteArray): String {
        return String(bytes, Charsets.UTF_8)
    }
}
