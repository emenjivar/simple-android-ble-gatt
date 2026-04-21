package com.emenjivar.simplebleclient.ui.main

import android.Manifest
import android.R
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresPermission
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emenjivar.simplebleclient.ble.BleConnectionState
import com.emenjivar.simplebleclient.ble.BluetoothDisabledException
import com.emenjivar.simplebleclient.permission.PermissionDeniedDialog
import com.emenjivar.simplebleclient.ui.theme.SimpleBLEClientTheme
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

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onRequestBluetoothEnable: (Intent) -> Unit,
    onClickDetail: (device: BluetoothDevice) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    MainScreen(
        uiState = uiState,
        onRequestBluetoothEnable = onRequestBluetoothEnable,
        onClickDetail = onClickDetail,
        onStartScan = viewModel::startScan,
        onStopScan = viewModel::stopScan,
        onConnect = viewModel::connect,
        onDisconnect = viewModel::disconnect
    )
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
@Stable
fun MainScreen(
    uiState: MainUiState,
    onRequestBluetoothEnable: (Intent) -> Unit,
    onClickDetail: (device: BluetoothDevice) -> Unit,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onConnect: (device: BluetoothDevice) -> Unit,
    onDisconnect: () -> Unit
) {
    val context = LocalContext.current
    val permissionState = rememberMultiplePermissionsState(permissions = permissions)
    val openPermissionDeniedDialog = remember { mutableStateOf(false) }
    var isScanning by remember { mutableStateOf(false) }

    LaunchedEffect(isScanning) {
        if (isScanning) {
            delay(5_000)
            onStopScan()
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
                    onStartScan()
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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                title = {
                    Text(
                        text = "PI commissioning",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
            )
        }
    ) { innerPadding ->
        Surface {
            AnimatedVisibility(
                modifier = Modifier.padding(top = innerPadding.calculateTopPadding()),
                visible = isScanning
            ) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
//                    item {
//                        Text(
//                            modifier = Modifier.padding(vertical = 20.dp),
//                            text = when {
//                                !permissionState.allPermissionsGranted -> "No permissions granted"
//                                isScanning -> "Scanning..."
//                                else -> "List of devices"
//                            },
//                            style = MaterialTheme.typography.headlineSmall,
//                        )
//                    }

                    item { Spacer(modifier = Modifier.height(12.dp)) }

                    items(uiState.pairedDevices.toList()) { device ->
                        DeviceItem(
                            name = device.name,
                            macAddress = device.address,
                            onClick = {
                                onClickDetail(device)
                            }
                        )
//                    Row(
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Column(modifier = Modifier.weight(1f)) {
//                            Text(
//                                text = "device: ${device.name}, address: ${device.address}"
//                            )
//                        }
//
//                        Column {
//                            Button(
//                                enabled = uiState.connectionState !is BleConnectionState.Connecting,
//                                onClick = {
//                                    if (uiState.connectionState.isConnected()) {
//                                        onDisconnect()
//                                    } else {
//                                        onConnect(device)
//                                    }
//                                }
//                            ) {
//                                Text(
//                                    text = if (uiState.connectionState.isConnected()) {
//                                        "disconnect"
//                                    } else {
//                                        "Connect"
//                                    }
//                                )
//                            }
//
//                            AnimatedVisibility(
//                                visible = uiState.connectionState.isConnected()
//                            ) {
//                                Button(
//                                    onClick = { onClickDetail(device.address) }
//                                ) {
//                                    Text("Open details")
//                                }
//                            }
//                        }
//                    }
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

@Preview
@Composable
private fun MainScreenPreview() {
    SimpleBLEClientTheme {
        MainScreen(
            uiState = MainUiState(),
            onRequestBluetoothEnable = {},
            onClickDetail = {},
            onStartScan = {},
            onStopScan = {},
            onConnect = {},
            onDisconnect = {}
        )
    }
}
