package com.emenjivar.simplebleclient.ui.detail

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue

@Composable
fun DetailScreen(
    route: DetailRoute,
    viewModel: DetailViewModel = hiltViewModel<DetailViewModel, DetailViewModel.Factory>(
        creationCallback = { factory -> factory.create(route) }
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DetailScreen(uiState = uiState)
}

@Composable
fun DetailScreen(
   uiState: DetailUiState
) {
    Text("macAddress: ${uiState.macAddress}")
    Text("ssid: ${uiState.ssid}")
    Text("ipAddress: ${uiState.ipAddress}")
}