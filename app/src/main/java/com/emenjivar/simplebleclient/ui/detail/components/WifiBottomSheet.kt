package com.emenjivar.simplebleclient.ui.detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.emenjivar.simplebleclient.R
import com.emenjivar.simplebleclient.ui.theme.SimpleBLEClientTheme
import com.emenjivar.simplebleclient.wifi.StateResult
import com.emenjivar.simplebleclient.wifi.WifiNetwork

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Stable
fun WifiBottomSheet(
    wifiScanResult: StateResult<List<WifiNetwork>>,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    onDismissRequest: () -> Unit
) {
    ModalBottomSheet(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        WifiBottomSheetLayout(
            modifier = Modifier.fillMaxWidth(),
            wifiScanResult = wifiScanResult
        )
    }
}

@Composable
@Stable
fun WifiBottomSheetLayout(
     wifiScanResult: StateResult<List<WifiNetwork>>,
     modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
    ) {
        LazyColumn(modifier = Modifier.padding(20.dp)) {
            item {
                Text(
                    text = "Select Wi-Fi Network",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = if(wifiScanResult is StateResult.Success) {
                        "Choose a network for your device"
                    } else {
                        "Scanning wifi networks..."
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            when (wifiScanResult) {
                StateResult.Loading -> {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
                is StateResult.Success -> {
                    itemsIndexed(wifiScanResult.data) { index, wifiNetwork ->
                        WifiNetworkItem(
                            modifier = Modifier
                                .padding(bottom = 8.dp),
                            wifiNetwork = wifiNetwork,
                            onClick = {}
                        )
                    }
                }
                StateResult.Idle -> {}
            }
        }
    }
}

@Composable
private fun WifiNetworkItem(
    wifiNetwork: WifiNetwork,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(start = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_wifi),
                contentDescription = null
            )

            Text(
                modifier = Modifier.weight(1f),
                text = wifiNetwork.ssid,
                style = MaterialTheme.typography.bodyLarge
            )

            IconButton(onClick = {}) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_right),
                    contentDescription = null
                )
            }
        }
    }
}

@Preview
@Composable
private fun WifiBottomSheetLoadingPreview() {
    SimpleBLEClientTheme {
        WifiBottomSheetLayout(
            wifiScanResult = StateResult.Loading
        )
    }
}

@Preview
@Composable
private fun WifiBottomSheetPreview() {
    SimpleBLEClientTheme {
        WifiBottomSheetLayout(
            wifiScanResult = StateResult.Success(
                data = listOf(
                    WifiNetwork(ssid = "Charlie network", rssi = -70),
                    WifiNetwork(ssid = "Office wifi", rssi = -60),
                    WifiNetwork(ssid = "Second floor", rssi = -50),
                    WifiNetwork(ssid = "Coffee shop free", rssi = -10),
                )
            )
        )
    }
}