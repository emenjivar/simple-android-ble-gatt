package com.emenjivar.simplebleclient.ble

import java.util.UUID

val primaryServiceUUID: UUID = UUID.fromString("290edf15-b540-4e83-83cf-ba647bf4df20")
val ledCharacteristicUUID: UUID = UUID.fromString("290edf15-b540-4e83-83cf-ba647bf4df21")

// Used for listening notification changes
val clientCharacteristicConfigUUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

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

        // Runs when each object in initialized, self-registering into a shared map.
        init {
            register(this)
        }

        companion object {
            // Shared across all read objects.
            // allowing lookups by characteristic
            private val _registry = mutableMapOf<UUID, Read<*>>()
            val registry: Map<UUID, Read<*>> get() = _registry

            internal fun register(command: Read<*>) {
                _registry[command.characteristic] = command
            }
        }
    }
}
