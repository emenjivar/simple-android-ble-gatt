package com.emenjivar.simplebleclient

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.emenjivar.simplebleclient.ui.theme.SimpleBLEClientTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SimpleBLEClientTheme {
                val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    listOf(
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT
                    )
                } else {
                    listOf(Manifest.permission.ACCESS_FINE_LOCATION)
                }
                val permissionState = rememberMultiplePermissionsState(permission)

                LaunchedEffect(permissionState) {
                    when {
                        permissionState.allPermissionsGranted -> {
                            Log.wtf("MainActivity", "all permission granted")
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
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
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