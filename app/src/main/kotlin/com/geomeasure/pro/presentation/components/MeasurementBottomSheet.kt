package com.geomeasure.pro.presentation.components

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    expanded: Boolean
) {
    val context = LocalContext.current
    val sortedVertices = remember(vertices) { vertices.sortedBy { it.order } }

    LazyColumn(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        item {
            Text("Measurement", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "${areaM2.formatDecimals(2)} m2",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("Area", style = MaterialTheme.typography.labelSmall)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "${perimeterM.formatDecimals(2)} m",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text("Perimeter", style = MaterialTheme.typography.labelSmall)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "${vertices.size}",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text("Vertices", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        if (expanded && areaM2 > 0) {
            item {
                Spacer(Modifier.height(16.dp))
                Divider()
                Spacer(Modifier.height(8.dp))
                Text("All Units", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(4.dp))
                val allUnits = UnitConverter.formatAllAreaUnits(areaM2)
                allUnits.forEach { (_, formatted) ->
                    Text(
                        formatted,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Spacer(Modifier.height(16.dp))
            Divider()
            Spacer(Modifier.height(8.dp))
            Text("Coordinates", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
        }

        itemsIndexed(sortedVertices) { index, vertex ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${index + 1}.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.width(24.dp)
                )
                Text(
                    vertex.latitude.formatDecimals(6),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    vertex.longitude.formatDecimals(6),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        if (sortedVertices.isNotEmpty()) {
            item {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
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
                        enabled = project != null
                    ) {
                        Text("Export GeoJSON")
                    }
                    Spacer(Modifier.width(8.dp))
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
                        modifier = Modifier.weight(1f),
                        enabled = project != null
                    ) {
                        Text("Share Text")
                    }
                }
            }
        }
    }
}
