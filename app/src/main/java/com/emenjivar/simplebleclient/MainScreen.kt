package com.emenjivar.simplebleclient

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emenjivar.simplebleclient.ble.BleNotifications
import com.emenjivar.simplebleclient.ble.BluetoothDisabledException
import com.emenjivar.simplebleclient.ble.CustomBluetoothManager
import com.emenjivar.simplebleclient.ble.LEDCommand
import com.emenjivar.simplebleclient.ble.WriteLed
import com.emenjivar.simplebleclient.permission.PermissionDeniedDialog
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.delay

private val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    listOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT
    )
} else {
    listOf(Manifest.permission.ACCESS_FINE_LOCATION)
}

@androidx.annotation.RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
@Stable
fun MainScreen(
    bluetoothManager: CustomBluetoothManager,
    bleNotifications: BleNotifications,
    onRequestBluetoothEnable: (Intent) -> Unit
) {
    val context = LocalContext.current
    val devices by bluetoothManager.pairedDevices.collectAsStateWithLifecycle()
    val connectedDevice by bluetoothManager.connectedDevice.collectAsStateWithLifecycle()
    val isConnecting by bluetoothManager.isConnecting.collectAsStateWithLifecycle()
    val ledState by bleNotifications.observe(WriteLed).collectAsStateWithLifecycle(LEDCommand.OFF)
    val permissionState = rememberMultiplePermissionsState(permissions = permissions)
    val openPermissionDeniedDialog = remember { mutableStateOf(false) }
    var isScanning by remember { mutableStateOf(false) }

    LaunchedEffect(isScanning) {
        if (isScanning) {
            delay(5_000)
            bluetoothManager.stopScan()
            isScanning = false
        }
    }

    LaunchedEffect(
        permissionState.allPermissionsGranted,
        permissionState.shouldShowRationale
    ) {
        when {
            permissionState.allPermissionsGranted -> {
                runCatching {
                    bluetoothManager.startScan()
                    isScanning = true
                }.onFailure { exception ->
                    if (exception is BluetoothDisabledException) {
                        val enabledBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        onRequestBluetoothEnable(enabledBluetoothIntent)
                    }
                }
            }

            permissionState.shouldShowRationale -> {
                // Permission denied by the user, but we can prompt again the permissions
                openPermissionDeniedDialog.value = true
            }

            else -> {
                // Never asked
                permissionState.launchMultiplePermissionRequest()
            }
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            item {
                Text(
                    text = when {
                        !permissionState.allPermissionsGranted -> "No permissions granted"
                        isScanning -> "Scanning in progress"
                        else -> "List of devices"
                    }
                )
            }

            items(devices.toList()) { device ->
                val isConnected = connectedDevice?.address == device.address
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f),) {
                        Text(
                            text = "device: ${device.name}, address: ${device.address}"
                        )
                        Text(text = "led state: $ledState")
                    }

                    Column {
                        Button(
                            enabled = !isConnecting,
                            onClick = {
                                if (isConnected) {
                                    bluetoothManager.disconnect()
                                } else {
                                    bluetoothManager.connect(device)
                                }
                            }
                        ) {
                            Text(
                                text = if (isConnected) {
                                    "disconnect"
                                } else {
                                    "Connect"
                                }
                            )
                        }

                        AnimatedVisibility(isConnected) {
                            Column {
                                Button(onClick = {
                                    bluetoothManager.readCharacteristic(WriteLed)
                                }) {
                                    Text(text = "Read characteristic")
                                }
                                Button(onClick = {
                                    val state = when (ledState) {
                                        LEDCommand.ON -> LEDCommand.OFF
                                        else -> LEDCommand.ON
                                    }
                                    bluetoothManager.writeCharacteristic(WriteLed, state)
                                }
                                ) {
                                    Text(text = if(ledState == LEDCommand.ON) "Turn OFF" else "Turn ON")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (openPermissionDeniedDialog.value) {
        PermissionDeniedDialog(
            onDismissRequest = { openPermissionDeniedDialog.value = false },
            openSettings = {
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }

                context.startActivity(intent)
            }
        )
    }
}
