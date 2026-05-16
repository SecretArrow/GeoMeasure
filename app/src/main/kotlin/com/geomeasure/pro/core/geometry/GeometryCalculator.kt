package com.geomeasure.pro.core.geometry

import kotlin.math.*

object GeometryCalculator {

    fun calculateArea(points: List<GeoPoint>): Double {
        if (points.size < 3) return 0.0
        val earthRadius = 6_378_137.0
        var area = 0.0
        val n = points.size
        for (i in 0 until n) {
            val j = (i + 1) % n
            val lat1 = Math.toRadians(points[i].latitude)
            val lat2 = Math.toRadians(points[j].latitude)
            val dLon = Math.toRadians(points[j].longitude - points[i].longitude)
            area += dLon * (2 + sin(lat1) + sin(lat2))
        }
        return abs(area) / 2.0 * earthRadius * earthRadius
    }

    fun calculatePerimeter(points: List<GeoPoint>): Double {
        if (points.size < 2) return 0.0
        var total = 0.0
        for (i in 0 until points.size - 1) {
            total += haversineDistance(points[i], points[i + 1])
        }
        if (points.size >= 3) total += haversineDistance(points.last(), points.first())
        return total
    }

    fun haversineDistance(a: GeoPoint, b: GeoPoint): Double {
        val R = 6_378_137.0
        val dLat = Math.toRadians(b.latitude - a.latitude)
        val dLon = Math.toRadians(b.longitude - a.longitude)
        val sinDLat = sin(dLat / 2)
        val sinDLon = sin(dLon / 2)
        val h = sinDLat * sinDLat +
                cos(Math.toRadians(a.latitude)) * cos(Math.toRadians(b.latitude)) *
                sinDLon * sinDLon
        return 2 * R * asin(sqrt(h.coerceIn(0.0, 1.0)))
    }
}

data class GeoPoint(val latitude: Double, val longitude: Double)
