package com.geomeasure.pro.data.export

import com.geomeasure.pro.core.util.escapeXml
import com.geomeasure.pro.data.local.db.entities.ProjectEntity
import com.geomeasure.pro.data.local.db.entities.VertexEntity

class GpxExporter {
    fun export(project: ProjectEntity, vertices: List<VertexEntity>): String {
        val waypoints = vertices.sortedBy { it.order }.mapIndexed { i, v ->
            """  <wpt lat="${v.latitude}" lon="${v.longitude}">
    <ele>${v.altitude}</ele>
    <name>Point ${i + 1}</name>
  </wpt>"""
        }.joinToString("\n")

        val trackPoints = vertices.sortedBy { it.order }.joinToString("\n") { v ->
            """      <trkpt lat="${v.latitude}" lon="${v.longitude}"><ele>${v.altitude}</ele></trkpt>"""
        }

        return """<?xml version="1.0" encoding="UTF-8"?>
<gpx version="1.1" creator="GeoMeasure Pro">
  <metadata><name>${project.name.escapeXml()}</name></metadata>
$waypoints
  <trk>
    <name>${project.name.escapeXml()}</name>
    <trkseg>
$trackPoints
    </trkseg>
  </trk>
</gpx>"""
    }
}
