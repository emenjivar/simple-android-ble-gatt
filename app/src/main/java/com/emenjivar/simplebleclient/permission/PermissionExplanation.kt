package com.emenjivar.simplebleclient.permission

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun PermissionsExplanationDialog(
    onRequestPermissions: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        title = {
            Text("Bluetooth permissions needed")
        },
        text = {
            Text(
                text = "Bluetooth permissions are needed to scan for nearby BLE devices and connect them to read and write data. Please accept the permissions to continue."
            )
        },
        onDismissRequest = {},
        confirmButton = {
            TextButton(onClick = onRequestPermissions) {
                Text("Allow")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
@Preview
private fun PermissionsExplanationDialogPreview() {
    PermissionsExplanationDialog(
        onRequestPermissions = {},
        onDismiss = {}
    )
}
