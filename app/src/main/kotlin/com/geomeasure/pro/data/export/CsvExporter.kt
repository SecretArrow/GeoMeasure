package com.geomeasure.pro.data.export

import com.geomeasure.pro.data.local.db.entities.ProjectEntity
import com.geomeasure.pro.data.local.db.entities.VertexEntity

class CsvExporter {

    private fun csvField(value: String): String {
        if (value.contains(',') || value.contains('"') || value.contains('\n') || value.contains('\r')) {
            return "\"${value.replace("\"", "\"\"")}\""
        }
        return value
    }

    fun export(project: ProjectEntity, vertices: List<VertexEntity>): String {
        val header = "Point,Latitude,Longitude,Altitude,Accuracy,Timestamp"
        val rows = vertices.sortedBy { it.order }.mapIndexed { i, v ->
            "${i + 1},${v.latitude},${v.longitude},${v.altitude},${v.gpsAccuracy ?: ""},${v.timestamp}"
        }
        val summary = listOf(
            "",
            "Project Name,${csvField(project.name)}",
            "Area (m2),${project.areaM2}",
            "Area (ha),${project.areaM2 * 1e-4}",
            "Area (acres),${project.areaM2 * 2.47105e-4}",
            "Perimeter (m),${project.perimeterM}",
            "Notes,${csvField(project.notes)}"
        )
        return (listOf(header) + rows + summary).joinToString("\n")
    }
}
