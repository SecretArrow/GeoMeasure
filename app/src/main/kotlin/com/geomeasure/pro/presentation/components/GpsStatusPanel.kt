package com.geomeasure.pro.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GpsStatusPanel(
    accuracyM: Float,
    satellitesUsed: Int,
    satellitesInView: Int,
    hdop: Float,
    altitudeM: Double
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GpsMetric(
                label = "Accuracy",
                value = "${accuracyM.toInt()} m",
                badgeColor = when {
                    accuracyM <= 5f -> Color(0xFF4CAF50)
                    accuracyM <= 10f -> Color(0xFFFF9800)
                    else -> Color(0xFFF44336)
                }
            )
            GpsMetric(
                label = "Satellites",
                value = "$satellitesUsed/$satellitesInView"
            )
            GpsMetric(
                label = "HDOP",
                value = "%.1f".format(hdop)
            )
            GpsMetric(
                label = "Altitude",
                value = "${altitudeM.toInt()} m"
            )
        }
    }
}

@Composable
private fun GpsMetric(
    label: String,
    value: String,
    badgeColor: Color? = null
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (badgeColor != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(badgeColor.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    value,
                    style = MaterialTheme.typography.titleSmall,
                    color = badgeColor,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Text(
                value,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(2.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
