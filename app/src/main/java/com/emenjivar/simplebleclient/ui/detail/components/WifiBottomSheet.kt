package com.emenjivar.simplebleclient.ui.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicSecureTextField
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.TextObfuscationMode
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.emenjivar.simplebleclient.R
import com.emenjivar.simplebleclient.ui.components.PrimaryButton
import com.emenjivar.simplebleclient.ui.theme.SimpleBLEClientTheme
import com.emenjivar.simplebleclient.wifi.StateResult
import com.emenjivar.simplebleclient.wifi.WifiNetwork
import kotlinx.coroutines.launch

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
        // dragHandle = null,
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        WifiBottomSheetLayout(
            modifier = Modifier.fillMaxWidth(),
            wifiScanResult = wifiScanResult
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Stable
fun WifiBottomSheetLayout(
     wifiScanResult: StateResult<List<WifiNetwork>>,
     modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState { 2 }
    var selectedWifi by remember { mutableStateOf<WifiNetwork?>(null) }
    val coroutineScope = rememberCoroutineScope()

    HorizontalPager(
        modifier = modifier,
        state = pagerState,
        userScrollEnabled = false
    ) { page ->
        when (page) {
            0 -> {
                SelectWifiLayout(
                    wifiScanResult = wifiScanResult,
                    onNetworkClick = { wifiNetwork ->
                        coroutineScope.launch {
                            selectedWifi = wifiNetwork
                            pagerState.animateScrollToPage(1)
                        }
                    }
                )
            }
            1 -> {
                EnterWifiPasswordLayout(
                    ssid = selectedWifi?.ssid.orEmpty(),
                    onBackClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(0)
                            selectedWifi = null
                        }
                    },
                    onConnectClick = {}
                )
            }
        }
    }
}

@Composable
private fun SelectWifiLayout(
    wifiScanResult: StateResult<List<WifiNetwork>>,
    modifier: Modifier = Modifier,
    onNetworkClick: (WifiNetwork) -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
    ) {
        LazyColumn(modifier = Modifier.padding(horizontal = 20.dp)) {
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
                            onClick = {
                                onNetworkClick(wifiNetwork)
                            }
                                // {
                                // coroutineScope.launch {
                                //     selectedWifi = wifiNetwork
                                //     pagerState.animateScrollToPage(1)
                                // }
                                //}
                        )
                    }

                    item {
                        // Extra space for scrolling
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
                StateResult.Idle -> {}
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnterWifiPasswordLayout(
    ssid: String,
    modifier: Modifier = Modifier,
    onConnectClick: () -> Unit,
    onBackClick: () -> Unit
) {
    var showPassword by remember { mutableStateOf(false) }
    val passwordState = remember { TextFieldState("") }
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
    ) {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            expandedHeight = 48.dp,
            title = {
                Text(
                    text = "Enter Wi-Fi Password",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = onBackClick
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_back),
                        contentDescription = null
                    )
                }
            },
        )

        Column(modifier = Modifier.padding(20.dp)) {
            ConnectedWifiItem(
                modifier = Modifier.fillMaxWidth(),
                ssid = ssid
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Password",
                style = MaterialTheme.typography.bodySmall
            )
            BasicSecureTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 10.dp),
                state = passwordState,
                textObfuscationMode = if (showPassword) {
                    TextObfuscationMode.Visible
                } else {
                    TextObfuscationMode.RevealLastTyped
                },
                decorator = { innerTextField ->
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                        innerTextField()

                        IconButton(
                            modifier = Modifier.align(Alignment.CenterEnd),
                            onClick = {
                                showPassword = !showPassword
                            }
                        ) {
                            Icon(
                                painter = painterResource(
                                    if (showPassword) R.drawable.ic_eye
                                    else R.drawable.ic_eye_off
                                ),
                                contentDescription = null
                            )
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            PrimaryButton(
                modifier = Modifier.fillMaxWidth(),
                text = "Connect",
                icon = R.drawable.ic_wifi,
                onClick = onConnectClick
            )
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
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

@Composable
private fun ConnectedWifiItem(
    ssid: String,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_wifi),
                contentDescription = null
            )

            Column {
                Text(
                    "Connected to:",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = ssid,
                    style = MaterialTheme.typography.bodyLarge
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

@Preview
@Composable
private fun EnterWifiPasswordLayoutPreview() {
    SimpleBLEClientTheme {
        EnterWifiPasswordLayout(
            ssid = "Charlie network",
            onConnectClick = {},
            onBackClick = {}
        )
    }
}