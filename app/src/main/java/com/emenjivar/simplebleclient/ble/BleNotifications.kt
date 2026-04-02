package com.emenjivar.simplebleclient.ble

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import java.util.UUID

interface BleNotifications {
    fun <T> emit(command: BleCommand.Read<T>, value: ByteArray)
    fun <T> observe(command: BleCommand.Read<T>): Flow<T>
}

class BleNotificationsImp : BleNotifications {
    private val _updates = MutableSharedFlow<Pair<UUID, ByteArray>>(
        replay = 1,
        extraBufferCapacity = 10,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override fun <T> emit(command: BleCommand.Read<T>, value: ByteArray) {
        _updates.tryEmit(command.characteristic to value)
    }

    override fun <T> observe(command: BleCommand.Read<T>): Flow<T> =
        _updates
            .filter { (uuid, _) -> uuid == command.characteristic }
            .map { (_, bytes) -> command.decode(bytes) }
}