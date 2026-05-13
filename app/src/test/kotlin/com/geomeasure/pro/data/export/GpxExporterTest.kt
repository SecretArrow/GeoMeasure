package com.geomeasure.pro.data.export

import com.geomeasure.pro.data.local.db.entities.ProjectEntity
import com.geomeasure.pro.data.local.db.entities.VertexEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GpxExporterTest {

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

    private val exporter = GpxExporter()

    @Test
    fun `export contains gpx root element`() {
        val result = exporter.export(testProject, testVertices)
        assertTrue(result.contains("<gpx"))
        assertTrue(result.contains("</gpx>"))
    }

    @Test
    fun `export contains wpt elements for each vertex`() {
        val result = exporter.export(testProject, testVertices)
        val wptCount = result.split("<wpt").count() - 1
        assertEquals(testVertices.size, wptCount)
    }

    @Test
    fun `export wpt elements have correct lat attribute`() {
        val result = exporter.export(testProject, testVertices)
        assertTrue(result.contains("""lat="0.0""""))
        assertTrue(result.contains("""lat="1.0""""))
    }

    @Test
    fun `export wpt elements have correct lon attribute`() {
        val result = exporter.export(testProject, testVertices)
        assertTrue(result.contains("""lon="0.0""""))
        assertTrue(result.contains("""lon="1.0""""))
    }

    @Test
    fun `export contains trk element`() {
        val result = exporter.export(testProject, testVertices)
        assertTrue(result.contains("<trk>"))
        assertTrue(result.contains("</trk>"))
    }

    @Test
    fun `export contains trkseg element`() {
        val result = exporter.export(testProject, testVertices)
        assertTrue(result.contains("<trkseg>"))
        assertTrue(result.contains("</trkseg>"))
    }

    @Test
    fun `export contains trkpt with correct lat lon`() {
        val result = exporter.export(testProject, testVertices)
        assertTrue(result.contains("""<trkpt lat="0.0" lon="0.0""""))
        assertTrue(result.contains("""<trkpt lat="1.0" lon="0.0""""))
        assertTrue(result.contains("""<trkpt lat="1.0" lon="1.0""""))
    }

    @Test
    fun `export has correct gpx version and creator`() {
        val result = exporter.export(testProject, testVertices)
        assertTrue(result.contains("""version="1.1""""))
        assertTrue(result.contains("""creator="GeoMeasure Pro""""))
    }

    @Test
    fun `export wpt includes name elements`() {
        val result = exporter.export(testProject, testVertices)
        assertTrue(result.contains("<name>Point 1</name>"))
        assertTrue(result.contains("<name>Point 2</name>"))
        assertTrue(result.contains("<name>Point 3</name>"))
    }

    @Test
    fun `export wpt includes elevation element`() {
        val result = exporter.export(testProject, testVertices)
        assertTrue(result.contains("<ele>0.0</ele>"))
    }
}
