package com.emenjivar.simplebleclient.ble

import java.util.UUID

val serviceUUID: UUID = UUID.fromString("290edf15-b540-4e83-83cf-ba647bf4df20")
val characteristicUUID: UUID = UUID.fromString("290edf15-b540-4e83-83cf-ba647bf4df21")

enum class LEDCommand(val value: Byte) {
    OFF(0x00),
    ON(0x01);

    companion object {
        fun fromValue(byte: Byte): LEDCommand? = entries.find { it.value == byte }
    }
}
