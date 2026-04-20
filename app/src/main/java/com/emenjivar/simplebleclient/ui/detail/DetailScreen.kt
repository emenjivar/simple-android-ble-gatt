package com.emenjivar.simplebleclient.ui.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.emenjivar.simplebleclient.ble.commands.LEDCommand

@Composable
fun DetailScreen(
    route: DetailRoute,
    viewModel: DetailViewModel = hiltViewModel<DetailViewModel, DetailViewModel.Factory>(
        creationCallback = { factory -> factory.create(route) }
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DetailScreen(
        uiState = uiState,
        onUpdateLedState = viewModel::updateLedState
    )
}

@Composable
fun DetailScreen(
   uiState: DetailUiState,
   onUpdateLedState: (LEDCommand) -> Unit
) {
    Scaffold { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            Text("macAddress: ${uiState.macAddress}")
            Text("ssid: ${uiState.ssid}")
            Text("ipAddress: ${uiState.ipAddress}")

            Button(onClick = {
                val state = when (uiState.ledState) {
                    LEDCommand.ON -> LEDCommand.OFF
                    else -> LEDCommand.ON
                }
                onUpdateLedState(state)
            }
            ) {
                Text(text = if (uiState.ledState == LEDCommand.ON) "Turn OFF" else "Turn ON")
            }
        }
    }
}