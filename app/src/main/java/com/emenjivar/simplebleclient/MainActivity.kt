package com.emenjivar.simplebleclient

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emenjivar.simplebleclient.ui.theme.SimpleBLEClientTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var bluetoothManager: CustomBluetoothManager

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bluetoothManager = CustomBluetoothManager(context = application)

        enableEdgeToEdge()
        setContent {
            val coroutineScope = rememberCoroutineScope()
            SimpleBLEClientTheme {
                val devices by bluetoothManager.pairedDevices.collectAsStateWithLifecycle()
                val connectedDevice by bluetoothManager.connectedDevice.collectAsStateWithLifecycle()

                val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    listOf(
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT
                    )
                } else {
                    listOf(Manifest.permission.ACCESS_FINE_LOCATION)
                }
                val permissionState = rememberMultiplePermissionsState(permission)
                var isScanning by remember { mutableStateOf(false) }

                LaunchedEffect(isScanning) {
                    if (isScanning) {
                        delay(5_000)
                        bluetoothManager.stopScan()
                        isScanning = false
                    }
                }

                LaunchedEffect(permissionState) {
                    when {
                        permissionState.allPermissionsGranted -> {
                            Log.wtf("MainActivity", "all permission granted")
                            bluetoothManager.startScan()
                            isScanning = true
                        }

                        permissionState.shouldShowRationale -> {
                            Log.wtf("MainActivity", "Some denied but can still ask")
                            permissionState.launchMultiplePermissionRequest()
                        }

                        // Never asked
                        else -> {
                            permissionState.launchMultiplePermissionRequest()
                        }
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LazyColumn (modifier = Modifier.padding(innerPadding)) {
                        item {
                            Text(
                                text = if (isScanning) {
                                    "Scanning in progress"
                                } else {
                                    "List of devices"
                                }
                            )
                        }

                        items(devices.toList()) { device ->
                            Row {
                                Text(
                                    modifier = Modifier.weight(1f),
                                    text = "device: ${device.name}, address: ${device.address}"
                                )
                                Button(
                                    onClick = {
                                        if (connectedDevice?.address == device.address) {
                                            bluetoothManager.disconnect()
                                        } else {
                                            bluetoothManager.connect(device)
                                        }
                                    }
                                ) {
                                    Text(
                                        text = if (connectedDevice?.address == device.address) {
                                            "disconnect"
                                        } else {
                                            "Connect"
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        bluetoothManager.stopScan()
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SimpleBLEClientTheme {
        Greeting("Android")
    }
}