package com.emenjivar.simplebleclient.ble

import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject

interface BleOperationQueue {
    fun enqueue(operation: () -> Unit)
    fun operationComplete()
    fun clear()
}

class BleOperationQueueImp @Inject constructor() : BleOperationQueue {
    private val commandQueue = ConcurrentLinkedQueue<() -> Unit>()
    private var isProcessing = false

    private fun processNext() {
        val next = commandQueue.poll()
        if (next != null) {
            isProcessing = true
            next()
        } else {
            isProcessing = false
        }
    }

    override fun enqueue(operation: () -> Unit) {
        commandQueue.add(operation)
        if (!isProcessing) processNext()
    }

    override fun operationComplete() {
        isProcessing = false
        processNext()
    }

    override fun clear() {
        commandQueue.clear()
        isProcessing = false
    }
}