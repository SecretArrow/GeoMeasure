package com.geomeasure.pro.data.export

import com.geomeasure.pro.data.local.db.entities.ProjectEntity
import com.geomeasure.pro.data.local.db.entities.VertexEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CsvExporterTest {

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

    private val exporter = CsvExporter()

    @Test
    fun `export has header row`() {
        val result = exporter.export(testProject, testVertices)
        val firstLine = result.lines().first()
        assertEquals("Point,Latitude,Longitude,Altitude,Accuracy,Timestamp", firstLine)
    }

    @Test
    fun `export has data rows matching vertex count`() {
        val result = exporter.export(testProject, testVertices)
        val dataRows = result.lines()
            .drop(1)
            .takeWhile { it.isNotEmpty() && it[0].isDigit() }
        assertEquals(testVertices.size, dataRows.size)
    }

    @Test
    fun `export data rows are comma-separated`() {
        val result = exporter.export(testProject, testVertices)
        val dataRows = result.lines()
            .drop(1)
            .takeWhile { it.isNotEmpty() && it[0].isDigit() }
        dataRows.forEach { row ->
            val columns = row.split(",")
            assertEquals(6, columns.size)
        }
    }

    @Test
    fun `export has summary section with area`() {
        val result = exporter.export(testProject, testVertices)
        assertTrue(result.contains("Area (m2),1000.0"))
        assertTrue(result.contains("Area (ha),0.1"))
        assertTrue(result.contains("Area (acres),0.247105"))
    }

    @Test
    fun `export has summary section with perimeter`() {
        val result = exporter.export(testProject, testVertices)
        assertTrue(result.contains("Perimeter (m),200.0"))
    }

    @Test
    fun `export has summary section with project name`() {
        val result = exporter.export(testProject, testVertices)
        assertTrue(result.contains("Project Name,Test Project"))
    }

    @Test
    fun `export has summary section with notes`() {
        val result = exporter.export(testProject, testVertices)
        assertTrue(result.contains("Notes,Test notes"))
    }

    @Test
    fun `export data rows have correct vertex indices`() {
        val result = exporter.export(testProject, testVertices)
        val dataRows = result.lines()
            .drop(1)
            .takeWhile { it.isNotEmpty() && it[0].isDigit() }
        dataRows.forEachIndexed { index, row ->
            assertTrue("Row ${index + 1} should start with ${index + 1}", row.startsWith("${index + 1},"))
        }
    }
}
