package com.emenjivar.simplebleclient.ble

import java.util.UUID

val serviceUUID: UUID = UUID.fromString("290edf15-b540-4e83-83cf-ba647bf4df20")
val characteristicUUID: UUID = UUID.fromString("290edf15-b540-4e83-83cf-ba647bf4df21")

enum class LEDCommand(val bytes: Byte) {
    OFF(0x00),
    ON(0x01);
}

sealed class BleCommand<T> {
    abstract val service: UUID
    abstract val characteristic: UUID

    abstract class Write<T>(
        override val service: UUID,
        override val characteristic: UUID,
    ): BleCommand<T>() {
        abstract fun encode(value: T): ByteArray
    }

    abstract class Read<T>(
        override val service: UUID,
        override val characteristic: UUID
    ): BleCommand<T>() {
        abstract fun decode(bytes: ByteArray): T
    }
}

object WriteLed : BleCommand.Write<LEDCommand>(
    service = UUID.fromString("290edf15-b540-4e83-83cf-ba647bf4df20"),
    characteristic = UUID.fromString("290edf15-b540-4e83-83cf-ba647bf4df21")
) {
    override fun encode(value: LEDCommand): ByteArray {
        return byteArrayOf(value.bytes)
    }
}

object ReadLed : BleCommand.Read<LEDCommand>(
    service = UUID.fromString("290edf15-b540-4e83-83cf-ba647bf4df20"),
    characteristic = UUID.fromString("290edf15-b540-4e83-83cf-ba647bf4df21")
) {
    override fun decode(bytes: ByteArray): LEDCommand {
        val decodedValue = LEDCommand.entries.find { it.bytes == bytes.firstOrNull() }
        if (decodedValue != null) {
            return decodedValue
        }

        throw IllegalArgumentException("Fail to decode $bytes")
    }
}
