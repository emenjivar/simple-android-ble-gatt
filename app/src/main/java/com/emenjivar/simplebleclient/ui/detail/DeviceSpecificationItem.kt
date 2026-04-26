package com.emenjivar.simplebleclient.ui.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.emenjivar.simplebleclient.ui.theme.SimpleBLEClientTheme

private const val ALPHA_TITLE = 0.8f

@Composable
fun DeviceSpecificationItem(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = 20.dp,
                vertical = 10.dp
            )
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.alpha(ALPHA_TITLE)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Preview
@Composable
private fun DeviceSpecificationItemPreview() {
    SimpleBLEClientTheme {
        DeviceSpecificationItem(
            title = "MAC ADDRESS",
            value = "AA:BB:CC:DD:EE:FF:11"
        )
    }
}