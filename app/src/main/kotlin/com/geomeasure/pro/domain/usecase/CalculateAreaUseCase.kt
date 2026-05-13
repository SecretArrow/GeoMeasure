package com.geomeasure.pro.domain.usecase

import com.geomeasure.pro.core.geometry.GeometryCalculator
import com.geomeasure.pro.core.geometry.GeoPoint
import com.geomeasure.pro.data.local.db.entities.VertexEntity
import javax.inject.Inject

class CalculateAreaUseCase @Inject constructor() {
    operator fun invoke(vertices: List<VertexEntity>): Pair<Double, Double> {
        val points = vertices.sortedBy { it.order }.map {
            GeoPoint(it.latitude, it.longitude)
        }
        val area = GeometryCalculator.calculateArea(points)
        val perimeter = GeometryCalculator.calculatePerimeter(points)
        return Pair(area, perimeter)
    }
}
