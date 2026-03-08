package com.emenjivar.simplebleclient.permission

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun PermissionDeniedDialog(
    onDismissRequest: () -> Unit,
    openSettings: () -> Unit
) {
    AlertDialog(
        title = {
            Text("Bluetooth permissions required")
        },
        text = {
            Text(
                text = "Bluetooth permissions are needed to scan for nearby BLE devices and connect them to read and write data. Please enable them in settings."
            )
        },
        onDismissRequest = {},
        confirmButton = {
            TextButton(onClick = openSettings) {
                Text("Open settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}

@Composable
@Preview
private fun PermissionDeniedDialogPreview() {
    PermissionDeniedDialog(
        onDismissRequest = {},
        openSettings = {}
    )
}