package com.geomeasure.pro.data.export

import com.geomeasure.pro.data.local.db.entities.ProjectEntity
import com.geomeasure.pro.data.local.db.entities.VertexEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KmlExporterTest {

    private val testProject = ProjectEntity(
        id = "test-id",
        name = "Test Project",
        notes = "Test notes",
        areaM2 = 1000.0,
        perimeterM = 200.0
    )

    private val testVertices = listOf(
        VertexEntity(projectId = "test-id", latitude = 0.0, longitude = 0.0, order = 0),
        VertexEntity(projectId = "test-id", latitude = 1.0, longitude = 0.0, order = 1),
        VertexEntity(projectId = "test-id", latitude = 1.0, longitude = 1.0, order = 2)
    )

    private val exporter = KmlExporter()

    @Test
    fun `export contains kml root element`() {
        val result = exporter.export(testProject, testVertices)
        assertTrue(result.contains("<kml"))
        assertTrue(result.contains("</kml>"))
    }

    @Test
    fun `export contains Placemark element`() {
        val result = exporter.export(testProject, testVertices)
        assertTrue(result.contains("<Placemark>"))
        assertTrue(result.contains("</Placemark>"))
    }

    @Test
    fun `export contains Polygon element`() {
        val result = exporter.export(testProject, testVertices)
        assertTrue(result.contains("<Polygon>"))
        assertTrue(result.contains("</Polygon>"))
    }

    @Test
    fun `export contains coordinates element`() {
        val result = exporter.export(testProject, testVertices)
        assertTrue(result.contains("<coordinates>"))
        assertTrue(result.contains("</coordinates>"))
    }

    @Test
    fun `export coordinates use lon lat alt format`() {
        val result = exporter.export(testProject, testVertices)
        assertTrue(result.contains("0.0,0.0,0.0"))
        assertTrue(result.contains("0.0,1.0,0.0"))
        assertTrue(result.contains("1.0,1.0,0.0"))
    }

    @Test
    fun `export escapeXml escapes special chars in name`() {
        val project = testProject.copy(name = "AT&T <Project>")
        val result = exporter.export(project, testVertices)
        assertTrue(result.contains("AT&amp;T &lt;Project&gt;"))
    }

    @Test
    fun `export escapeXml escapes special chars in notes`() {
        val project = testProject.copy(notes = "notes & stuff <more>")
        val result = exporter.export(project, testVertices)
        assertTrue(result.contains("notes &amp; stuff &lt;more&gt;"))
    }

    @Test
    fun `export starts with XML declaration`() {
        val result = exporter.export(testProject, testVertices)
        assertTrue(result.startsWith("""<?xml version="1.0" encoding="UTF-8"?>"""))
    }

    @Test
    fun `export has kml namespace`() {
        val result = exporter.export(testProject, testVertices)
        assertTrue(result.contains("""xmlns="http://www.opengis.net/kml/2.2""""))
    }
}
