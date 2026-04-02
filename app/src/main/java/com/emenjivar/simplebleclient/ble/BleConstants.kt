package com.emenjivar.simplebleclient.ble

import java.util.UUID

val serviceUUID: UUID = UUID.fromString("290edf15-b540-4e83-83cf-ba647bf4df20")
val characteristicUUID: UUID = UUID.fromString("290edf15-b540-4e83-83cf-ba647bf4df21")

enum class LEDCommand(val bytes: Byte) {
    OFF(0x00),
    ON(0x01);

    companion object {
        fun fromValue(byte: Byte): LEDCommand? = entries.find { it.bytes == byte }
    }
}

abstract class BleCommand<T>(
    open val service: UUID,
    open val characteristic: UUID
) {
    abstract fun encode(value: T): ByteArray
    abstract fun decode(bytes: ByteArray): T
}

//sealed class BleOperation<T> {
//    abstract val command: BleCommand<T>
//
//    open class Write<T>(override val command: BleCommand<T>, val value: T): BleOperation<T>()
//    open class Read<T>(override val command: BleCommand<T>): BleOperation<T>()
//}

/**
 * Used for read and write the state of the led
 * Useful in this context where the BLE characteristic is used for both read and write
 */
object WriteLed : BleCommand<LEDCommand>(
    service = UUID.fromString("290edf15-b540-4e83-83cf-ba647bf4df20"),
    characteristic = UUID.fromString("290edf15-b540-4e83-83cf-ba647bf4df21")
) {
    override fun encode(value: LEDCommand): ByteArray {
        return byteArrayOf(value.bytes)
    }

    override fun decode(bytes: ByteArray): LEDCommand {
        val decodedValue = LEDCommand.entries.find { it.bytes == bytes.firstOrNull() }
        if (decodedValue != null) {
            return decodedValue
        }

        throw IllegalArgumentException("Fail to decode $bytes")
    }
}

