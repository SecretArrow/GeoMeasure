package com.geomeasure.pro.presentation.components

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.geomeasure.pro.core.util.UnitConverter
import com.geomeasure.pro.core.util.formatDecimals
import com.geomeasure.pro.data.export.GeoJsonExporter
import com.geomeasure.pro.data.local.db.entities.ProjectEntity
import com.geomeasure.pro.data.local.db.entities.VertexEntity

@Composable
fun MeasurementBottomSheet(
    areaM2: Double,
    perimeterM: Double,
    vertices: List<VertexEntity>,
    project: ProjectEntity? = null,
    expanded: Boolean,
    onShareScreenshot: () -> Unit = {}
) {
    val context = LocalContext.current
    val sortedVertices = remember(vertices) { vertices.sortedBy { it.order } }

    LazyColumn(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp)) {
        item {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.SquareFoot,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    project?.name ?: "Measurement",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricBox(
                    value = "${areaM2.formatDecimals(2)} m2",
                    label = "Area",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                MetricBox(
                    value = "${perimeterM.formatDecimals(2)} m",
                    label = "Perimeter",
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
                MetricBox(
                    value = "${vertices.size}",
                    label = "Vertices",
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (expanded && areaM2 > 0) {
            item {
                Spacer(Modifier.height(20.dp))
                Divider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(12.dp))
                Text(
                    "All Units",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                val allUnits = UnitConverter.formatAllAreaUnits(areaM2)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    allUnits.forEach { (_, formatted) ->
                        Text(
                            formatted,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(20.dp))
            Divider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(12.dp))
            Text(
                "Coordinates",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "#",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(24.dp)
                )
                Text(
                    "Latitude",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "Longitude",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(4.dp))
        }

        itemsIndexed(sortedVertices) { index, vertex ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${index + 1}.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(24.dp)
                )
                Text(
                    vertex.latitude.formatDecimals(6),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    vertex.longitude.formatDecimals(6),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (sortedVertices.isNotEmpty()) {
            item {
                Spacer(Modifier.height(16.dp))
                Divider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (project != null) {
                                val content = GeoJsonExporter().export(project, sortedVertices)
                                val file = java.io.File(context.cacheDir, "${project.name}.geojson")
                                file.parentFile?.mkdirs()
                                file.writeText(content)
                                val uri = androidx.core.content.FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    file
                                )
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/geo+json"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(intent, "Export GeoJSON"))
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = project != null,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.IosShare, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("GeoJSON")
                    }
                    OutlinedButton(
                        onClick = onShareScreenshot,
                        modifier = Modifier.weight(1f),
                        enabled = project != null,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Screenshot")
                    }
                }
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = {
                        if (project != null) {
                            val shareText = buildString {
                                appendLine("${project.name}")
                                appendLine("Area: ${areaM2.formatDecimals(2)} m2")
                                appendLine("Perimeter: ${perimeterM.formatDecimals(2)} m")
                                appendLine("Vertices: ${vertices.size}")
                                sortedVertices.forEachIndexed { i, v ->
                                    appendLine("${i + 1}. ${v.latitude.formatDecimals(6)}, ${v.longitude.formatDecimals(6)}")
                                }
                            }
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share"))
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = project != null,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Share Text")
                }
            }
        }
    }
}

@Composable
private fun MetricBox(
    value: String,
    label: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(2.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
