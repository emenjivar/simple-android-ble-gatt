package com.emenjivar.simplebleclient.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

interface BleScanner {
    val pairedDevices: StateFlow<List<BluetoothDevice>>
    fun startScan()
    fun stopScan()
}

class BleScannerImp(
    context: Context
) : BleScanner {

    private val bluetoothManager: BluetoothManager = context.getSystemService(BluetoothManager::class.java)
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

    override val pairedDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())

    override fun startScan() {
        if (bluetoothAdapter == null) {
            throw Exception("Bluetooth not supported")
        }

        if (!bluetoothAdapter.isEnabled) {
            // Bluetooth was manually disabled, prompt the user to enable it
            throw BluetoothDisabledException()
        }

        val filter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid.fromString("290edf15-b540-4e83-83cf-ba647bf4df20"))
            .build()
        val setting = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        pairedDevices.update { emptyList() }
        bluetoothAdapter.bluetoothLeScanner?.startScan(listOf(filter), setting, scanCallback)
    }

    override fun stopScan() {
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)

            if (result?.device != null) {
                pairedDevices.update { current ->
                    val exist = current.any { it.address == result.device.address }
                    if (exist) {
                        current.map { if (it.address == result.device.address) result.device else it }
                    } else {
                        current + result.device
                    }
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.wtf("MainActivity", "Scan faiiled: $errorCode")
        }
    }
}