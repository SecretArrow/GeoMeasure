package com.geomeasure.pro.domain.usecase

import com.geomeasure.pro.data.local.db.entities.VertexEntity
import org.junit.Assert.*
import org.junit.Test

class CalculateAreaUseCaseTest {
    private val useCase = CalculateAreaUseCase()

    @Test
    fun `empty vertices returns zero`() {
        val (area, perimeter) = useCase(emptyList())
        assertEquals(0.0, area, 0.001)
        assertEquals(0.0, perimeter, 0.001)
    }

    @Test
    fun `three vertices returns non-zero area`() {
        val vertices = listOf(
            VertexEntity(projectId = "p1", latitude = 0.0, longitude = 0.0, order = 0),
            VertexEntity(projectId = "p1", latitude = 0.001, longitude = 0.0, order = 1),
            VertexEntity(projectId = "p1", latitude = 0.001, longitude = 0.001, order = 2)
        )
        val (area, perimeter) = useCase(vertices)
        assertTrue("Area should be positive", area > 0.0)
        assertTrue("Perimeter should be positive", perimeter > 0.0)
    }

    @Test
    fun `vertices sorted by order`() {
        val vertices = listOf(
            VertexEntity(projectId = "p1", latitude = 1.0, longitude = 1.0, order = 2),
            VertexEntity(projectId = "p1", latitude = 0.0, longitude = 0.0, order = 0),
            VertexEntity(projectId = "p1", latitude = 0.0, longitude = 1.0, order = 1)
        )
        val (area, _) = useCase(vertices)
        assertTrue("Area should be positive regardless of order", area > 0.0)
    }
}
