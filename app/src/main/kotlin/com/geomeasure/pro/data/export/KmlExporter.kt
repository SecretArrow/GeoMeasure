package com.geomeasure.pro.data.export

import com.geomeasure.pro.core.util.escapeXml
import com.geomeasure.pro.data.local.db.entities.ProjectEntity
import com.geomeasure.pro.data.local.db.entities.VertexEntity

class KmlExporter {
    fun export(project: ProjectEntity, vertices: List<VertexEntity>): String {
        val sorted = vertices.sortedBy { it.order }
        val coords = sorted.joinToString(" ") { "${it.longitude},${it.latitude},${it.altitude}" }
        // Close the polygon ring by repeating the first coordinate
        val closedCoords = if (sorted.isNotEmpty()) {
            val first = sorted.first()
            "$coords ${first.longitude},${first.latitude},${first.altitude}"
        } else {
            coords
        }

        return """<?xml version="1.0" encoding="UTF-8"?>
<kml xmlns="http://www.opengis.net/kml/2.2">
  <Placemark>
    <name>${project.name.escapeXml()}</name>
    <description>Area: ${project.areaM2} m2&#10;Perimeter: ${project.perimeterM} m&#10;${project.notes.escapeXml()}</description>
    <Polygon>
      <outerBoundaryIs>
        <LinearRing>
          <coordinates>$closedCoords</coordinates>
        </LinearRing>
      </outerBoundaryIs>
    </Polygon>
  </Placemark>
</kml>"""
    }
}
