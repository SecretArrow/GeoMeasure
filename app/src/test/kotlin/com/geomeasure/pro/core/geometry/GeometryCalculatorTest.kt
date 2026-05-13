package com.geomeasure.pro.core.geometry

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GeometryCalculatorTest {

    @Test
    fun `calculateArea with 0 points returns 0`() {
        assertEquals(0.0, GeometryCalculator.calculateArea(emptyList()), 0.0)
    }

    @Test
    fun `calculateArea with 1 point returns 0`() {
        assertEquals(0.0, GeometryCalculator.calculateArea(listOf(GeoPoint(0.0, 0.0))), 0.0)
    }

    @Test
    fun `calculateArea with 2 points returns 0`() {
        assertEquals(0.0, GeometryCalculator.calculateArea(listOf(
            GeoPoint(0.0, 0.0), GeoPoint(1.0, 0.0)
        )), 0.0)
    }

    @Test
    fun `calculateArea with 3 points small triangle returns non-zero`() {
        val area = GeometryCalculator.calculateArea(listOf(
            GeoPoint(0.0, 0.0),
            GeoPoint(0.0, 1.0),
            GeoPoint(1.0, 0.0)
        ))
        assertTrue("Area should be positive for a triangle", area > 0.0)
    }

    @Test
    fun `calculateArea with 4 points square returns expected area`() {
        val area = GeometryCalculator.calculateArea(listOf(
            GeoPoint(0.0, 0.0),
            GeoPoint(0.0, 1.0),
            GeoPoint(1.0, 1.0),
            GeoPoint(1.0, 0.0)
        ))
        assertEquals(1.24e10, area, 1e9)
    }

    @Test
    fun `calculatePerimeter with 0 points returns 0`() {
        assertEquals(0.0, GeometryCalculator.calculatePerimeter(emptyList()), 0.0)
    }

    @Test
    fun `calculatePerimeter with 1 point returns 0`() {
        assertEquals(0.0, GeometryCalculator.calculatePerimeter(listOf(GeoPoint(0.0, 0.0))), 0.0)
    }

    @Test
    fun `calculatePerimeter with 2 points returns haversine distance`() {
        val a = GeoPoint(0.0, 0.0)
        val b = GeoPoint(0.0, 1.0)
        val expected = GeometryCalculator.haversineDistance(a, b)
        assertEquals(expected, GeometryCalculator.calculatePerimeter(listOf(a, b)), 0.001)
    }

    @Test
    fun `calculatePerimeter with 3 plus points closes polygon`() {
        val a = GeoPoint(0.0, 0.0)
        val b = GeoPoint(0.0, 1.0)
        val c = GeoPoint(1.0, 1.0)
        val edgesOnly = GeometryCalculator.haversineDistance(a, b) +
            GeometryCalculator.haversineDistance(b, c)
        val perimeter = GeometryCalculator.calculatePerimeter(listOf(a, b, c))
        val closure = GeometryCalculator.haversineDistance(c, a)
        assertEquals(edgesOnly + closure, perimeter, 0.001)
    }

    @Test
    fun `haversineDistance with same point returns 0`() {
        assertEquals(0.0, GeometryCalculator.haversineDistance(
            GeoPoint(0.0, 0.0), GeoPoint(0.0, 0.0)
        ), 0.0)
    }

    @Test
    fun `haversineDistance known distance with Jakarta coordinates`() {
        val a = GeoPoint(-6.2088, 106.8456)
        val b = GeoPoint(-6.2088, 106.9456)
        val distance = GeometryCalculator.haversineDistance(a, b)
        assertEquals(11069.0, distance, 10.0)
    }
}
