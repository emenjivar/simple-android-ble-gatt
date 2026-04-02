package com.emenjivar.simplebleclient

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.emenjivar.simplebleclient.ui.theme.SimpleBLEClientTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    @androidx.annotation.RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SimpleBLEClientTheme {
                MainScreen(
                    viewModel = viewModel,
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
                    runCatching { viewModel.startScan() }
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
        viewModel.stopScan()
    }
}
