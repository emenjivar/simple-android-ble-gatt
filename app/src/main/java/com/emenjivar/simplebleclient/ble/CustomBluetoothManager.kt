package com.emenjivar.simplebleclient.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.os.ParcelUuid
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class CustomBluetoothManager(private val context: Context) {
    private var bluetoothGatt: BluetoothGatt? = null
    private val bluetoothManager: BluetoothManager = context.getSystemService(BluetoothManager::class.java)
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

    private val _pairedDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val pairedDevices = _pairedDevices.asStateFlow()

    private val _connectedDevice = MutableStateFlow<BluetoothDevice?>(null)
    val connectedDevice = _connectedDevice.asStateFlow()

    private val _isConnecting = MutableStateFlow(false)
    val isConnecting = _isConnecting.asStateFlow()

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)

            if (result?.device != null) {
                _pairedDevices.update { current ->
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

    fun startScan() {
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
        _pairedDevices.update { emptyList() }
        bluetoothAdapter.bluetoothLeScanner?.startScan(listOf(filter), setting, scanCallback)
    }

    fun stopScan() {
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            Log.d("BLE", "onConnectionStateChange status=$status newState=$newState")
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.wtf("CustomBluetoothManager", "connected to ${gatt?.device?.address}")
                    _connectedDevice.update { gatt?.device}
                    gatt?.discoverServices()
                    _isConnecting.update { false }
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.wtf("CustomBluetoothManager", "disconnected to ${gatt?.device?.address}, status: $status")
                    bluetoothGatt?.close()
                    _connectedDevice.update { null }
                    bluetoothGatt = null
                    _isConnecting.update { false }
                }
                BluetoothProfile.STATE_CONNECTING, BluetoothProfile.STATE_DISCONNECTING -> {
                    _isConnecting.update { true }
                }
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, value, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val value = value.toString(Charsets.UTF_8)
                Log.wtf("CustomBluetoothManager", "*read value: $value")
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val value = characteristic?.value?.toString(Charsets.UTF_8)
                Log.wtf("CustomBluetoothManager", "read value: $value")
            }
        }
    }

    fun connect(device: BluetoothDevice) {
        _isConnecting.update { true }
        stopScan()
        bluetoothGatt = device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
    }

    fun disconnect() {
        _isConnecting.update { true }
        bluetoothGatt?.disconnect()
    }

    private val SERVICE_UUID = UUID.fromString("290edf15-b540-4e83-83cf-ba647bf4df20")
    private val CHARACTERISTIC_UUID = UUID.fromString("290edf15-b540-4e83-83cf-ba647bf4df21")

    fun readCharacteristic() {
        val characteristic = bluetoothGatt
            ?.getService(SERVICE_UUID)
            ?.getCharacteristic(CHARACTERISTIC_UUID)

        bluetoothGatt?.readCharacteristic(characteristic)
    }
}