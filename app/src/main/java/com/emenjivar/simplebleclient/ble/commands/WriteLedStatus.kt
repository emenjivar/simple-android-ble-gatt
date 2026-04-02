package com.emenjivar.simplebleclient.ble.commands

import com.emenjivar.simplebleclient.ble.BleCommand
import com.emenjivar.simplebleclient.ble.ledCharacteristicUUID
import com.emenjivar.simplebleclient.ble.primaryServiceUUID

/**
 * Turn on/off the LED
 */
object WriteLedStatus : BleCommand.Write<LEDCommand>(
    service = primaryServiceUUID,
    characteristic = ledCharacteristicUUID
) {
    override fun encode(value: LEDCommand): ByteArray {
        return byteArrayOf(value.bytes)
    }
}