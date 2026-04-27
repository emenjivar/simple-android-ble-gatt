package com.emenjivar.simplebleclient.permission

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.emenjivar.simplebleclient.ui.theme.SimpleBLEClientTheme

@Composable
fun LocationPermissionDeniedDialog(
    onDismissRequest: () -> Unit,
    openSettings: () -> Unit
) {
    AlertDialog(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        title = {
            Text(
                text = "Location permissions required"
            )
        },
        text = {
            Text(
                text = "Location access is required to scan for nearby Wi-Fi networks. Please go to setting and enable location permissions to continue"
            )
        },
        onDismissRequest = onDismissRequest,
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
private fun LocationPermissionDeniedDialogPreview() {
    SimpleBLEClientTheme {
        LocationPermissionDeniedDialog(
            onDismissRequest = {},
            openSettings = {}
        )
    }
}