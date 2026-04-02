package com.emenjivar.simplebleclient

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.emenjivar.simplebleclient.ble.BleNotificationsImp
import com.emenjivar.simplebleclient.ble.CustomBluetoothManager
import com.emenjivar.simplebleclient.ui.theme.SimpleBLEClientTheme

class MainActivity : ComponentActivity() {
    private lateinit var bluetoothManager: CustomBluetoothManager
    private val bleNotifications = BleNotificationsImp()

    @androidx.annotation.RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bluetoothManager = CustomBluetoothManager(
            context = application,
            bleNotifications = bleNotifications
        )

        enableEdgeToEdge()
        setContent {
            SimpleBLEClientTheme {
                MainScreen(
                    bluetoothManager = bluetoothManager,
                    bleNotifications = bleNotifications,
                    onRequestBluetoothEnable = { intent ->
                        enableBluetoothLaunched.launch(intent)
                    }
                )
            }
        }
    }

    private val enableBluetoothLaunched =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                RESULT_OK -> {
                    runCatching { bluetoothManager.startScan() }
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
