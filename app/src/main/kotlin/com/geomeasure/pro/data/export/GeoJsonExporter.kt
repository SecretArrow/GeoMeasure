package com.geomeasure.pro.data.export

import com.geomeasure.pro.core.util.escapeJson
import com.geomeasure.pro.data.local.db.entities.ProjectEntity
import com.geomeasure.pro.data.local.db.entities.VertexEntity

class GeoJsonExporter {
    fun export(project: ProjectEntity, vertices: List<VertexEntity>): String {
        val coordinates = vertices.sortedBy { it.order }
            .map { "[${it.longitude}, ${it.latitude}]" }
            .let { if (it.isNotEmpty()) it + it.first() else it }

        return """
        {
          "type": "FeatureCollection",
          "features": [{
            "type": "Feature",
            "geometry": {
              "type": "Polygon",
              "coordinates": [[${coordinates.joinToString(",")}]]
            },
            "properties": {
              "name": "${project.name.escapeJson()}",
              "notes": "${project.notes.escapeJson()}",
              "area_m2": ${project.areaM2},
              "perimeter_m": ${project.perimeterM},
              "created_at": "${project.createdAt}",
              "modified_at": "${project.modifiedAt}"
            }
          }]
        }
        """.trimIndent()
    }
}
