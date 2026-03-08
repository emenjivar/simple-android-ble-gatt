package com.emenjivar.simplebleclient

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emenjivar.simplebleclient.permission.PermissionDeniedDialog
import com.emenjivar.simplebleclient.permission.PermissionsExplanationDialog
import com.emenjivar.simplebleclient.ui.theme.SimpleBLEClientTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private lateinit var bluetoothManager: CustomBluetoothManager

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bluetoothManager = CustomBluetoothManager(context = application)

        enableEdgeToEdge()
        setContent {
            val coroutineScope = rememberCoroutineScope()
            // Prompt system settings to manually grant access to the permissions
            val openPermissionDeniedDialog = remember { mutableStateOf(false) }

            SimpleBLEClientTheme {
                val devices by bluetoothManager.pairedDevices.collectAsStateWithLifecycle()
                val connectedDevice by bluetoothManager.connectedDevice.collectAsStateWithLifecycle()
                val isConnecting by bluetoothManager.isConnecting.collectAsStateWithLifecycle()

                val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    listOf(
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT
                    )
                } else {
                    listOf(Manifest.permission.ACCESS_FINE_LOCATION)
                }
                val permissionState = rememberMultiplePermissionsState(permissions = permission)
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
                            bluetoothManager.startScan(
                                onSuccess = { isScanning = true },
                                promptEnableBluetooth = { intent, _ ->
                                    enableBluetoothLaunched.launch(intent)
                                }
                            )
                        }

                        permissionState.shouldShowRationale -> {
                            // Permission denied by the user, but we can prompt again the permissions
                            openPermissionDeniedDialog.value = true
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
                                Text(
                                    modifier = Modifier.weight(1f),
                                    text = "device: ${device.name}, address: ${device.address}"
                                )

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
                                        Button(onClick = { bluetoothManager.readCharacteristic() }) {
                                            Text(text = "Read characteristic")
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
                                data = Uri.fromParts("package", applicationContext.packageName, null)
                            }

                            startActivity(intent)
                        }
                    )
                }
            }
        }
    }

    private val enableBluetoothLaunched = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        when (result.resultCode) {
            RESULT_OK -> {
                // Is it possible to launch scanning here?
                Toast.makeText(
                    applicationContext,
                    "Bluetooth enabled", Toast.LENGTH_SHORT
                ).show()
            }
            RESULT_CANCELED -> {
                Toast.makeText(
                    applicationContext,
                    "Grant all the permissions to continue", Toast.LENGTH_SHORT
                ).show()
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