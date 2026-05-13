package com.geomeasure.pro.data.export

import com.geomeasure.pro.core.util.escapeXml
import com.geomeasure.pro.data.local.db.entities.ProjectEntity
import com.geomeasure.pro.data.local.db.entities.VertexEntity

class KmlExporter {
    fun export(project: ProjectEntity, vertices: List<VertexEntity>): String {
        val coords = vertices.sortedBy { it.order }
            .joinToString(" ") { "${it.longitude},${it.latitude},${it.altitude}" }

        return """<?xml version="1.0" encoding="UTF-8"?>
<kml xmlns="http://www.opengis.net/kml/2.2">
  <Placemark>
    <name>${project.name.escapeXml()}</name>
    <description>Area: ${project.areaM2} m2&#10;Perimeter: ${project.perimeterM} m&#10;${project.notes.escapeXml()}</description>
    <Polygon>
      <outerBoundaryIs>
        <LinearRing>
          <coordinates>$coords</coordinates>
        </LinearRing>
      </outerBoundaryIs>
    </Polygon>
  </Placemark>
</kml>"""
    }
}
