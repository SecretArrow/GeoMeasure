package com.geomeasure.pro.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.geomeasure.pro.core.util.UnitConverter
import com.geomeasure.pro.core.util.formatDecimals
import com.geomeasure.pro.data.local.db.entities.VertexEntity

@Composable
fun UnitConversionCard(
    squareMetres: Double,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().padding(8.dp)) {
        Text("Unit Conversions", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(4.dp))
        val allUnits = UnitConverter.formatAllAreaUnits(squareMetres)
        allUnits.forEach { (_, formatted) ->
            Text(
                formatted,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
