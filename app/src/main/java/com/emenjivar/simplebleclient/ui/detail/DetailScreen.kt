package com.emenjivar.simplebleclient.ui.detail

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun DetailScreen(macAddress: String) {
    Text("macAddress: $macAddress")
}