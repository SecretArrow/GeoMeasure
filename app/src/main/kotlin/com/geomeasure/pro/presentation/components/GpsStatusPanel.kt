package com.geomeasure.pro.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun GpsStatusPanel(
    accuracyM: Float,
    satellitesUsed: Int,
    satellitesInView: Int,
    hdop: Float,
    altitudeM: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GpsMetric(
                label = "Accuracy",
                value = "${accuracyM.toInt()} m",
                color = when {
                    accuracyM <= 5f -> Color.Green
                    accuracyM <= 10f -> Color(0xFFFF9800)
                    else -> Color.Red
                }
            )
            GpsMetric(label = "Satellites", value = "$satellitesUsed/$satellitesInView")
            GpsMetric(label = "HDOP", value = "%.1f".format(hdop))
            GpsMetric(label = "Altitude", value = "${altitudeM.toInt()} m")
        }
    }
}

@Composable
fun GpsMetric(
    label: String,
    value: String,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            color = color
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
