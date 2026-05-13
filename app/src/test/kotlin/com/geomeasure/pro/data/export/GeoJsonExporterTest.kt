package com.geomeasure.pro.data.export

import com.geomeasure.pro.data.local.db.entities.ProjectEntity
import com.geomeasure.pro.data.local.db.entities.VertexEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GeoJsonExporterTest {

    private val testProject = ProjectEntity(
        id = "test-id",
        name = "Test Project",
        notes = "Test notes",
        areaM2 = 1000.0,
        perimeterM = 200.0
    )

    private val testVertices = listOf(
        VertexEntity(projectId = "test-id", latitude = 0.0, longitude = 0.0, order = 0),
        VertexEntity(projectId = "test-id", latitude = 1.0, longitude = 1.0, order = 1)
    )

    private val exporter = GeoJsonExporter()

    @Test
    fun `export creates valid JSON structure`() {
        val result = exporter.export(testProject, testVertices)
        assertTrue(result.startsWith("{"))
        assertTrue(result.endsWith("}"))
    }

    @Test
    fun `export contains type FeatureCollection`() {
        val result = exporter.export(testProject, testVertices)
        assertTrue(result.contains(""""type": "FeatureCollection""""))
    }

    @Test
    fun `export contains coordinates array`() {
        val result = exporter.export(testProject, testVertices)
        assertTrue(result.contains(""""coordinates""""))
        assertTrue(result.contains("["))
    }

    @Test
    fun `export contains properties`() {
        val result = exporter.export(testProject, testVertices)
        assertTrue(result.contains(""""properties""""))
        assertTrue(result.contains(""""name": "Test Project""""))
        assertTrue(result.contains(""""area_m2": 1000.0"""))
    }

    @Test
    fun `export handles empty vertices list`() {
        val result = exporter.export(testProject, emptyList())
        assertTrue(result.contains(""""type": "FeatureCollection""""))
        assertTrue(result.contains(""""coordinates""""))
    }

    @Test
    fun `export coordinates are in lon lat order`() {
        val result = exporter.export(testProject, testVertices)
        assertTrue(result.contains("[0.0, 0.0]"))
        assertTrue(result.contains("[1.0, 1.0]"))
    }

    @Test
    fun `export closes polygon ring`() {
        val result = exporter.export(testProject, testVertices)
        val coords = listOf(
            VertexEntity(projectId = "test-id", latitude = 0.0, longitude = 0.0, order = 0),
            VertexEntity(projectId = "test-id", latitude = 1.0, longitude = 1.0, order = 1)
        )
        val expectedCount = coords.size + 1
        val matches = Regex("""\[\d+\.?\d*, \d+\.?\d*\]""").findAll(result).count()
        assertEquals(expectedCount, matches)
    }
}
