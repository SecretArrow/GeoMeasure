package com.geomeasure.pro.core.util

import com.geomeasure.pro.core.util.UnitConverter.AreaUnit
import com.geomeasure.pro.core.util.UnitConverter.DistanceUnit
import org.junit.Assert.assertEquals
import org.junit.Test

class UnitConverterTest {

    @Test
    fun `convertArea square metres to hectares`() {
        assertEquals(1.0, UnitConverter.convertArea(10000.0, AreaUnit.HECTARE), 0.001)
    }

    @Test
    fun `convertArea square metres to square metres`() {
        assertEquals(1.0, UnitConverter.convertArea(1.0, AreaUnit.SQUARE_METRE), 0.001)
    }

    @Test
    fun `convertArea square metres to acres`() {
        assertEquals(1.0, UnitConverter.convertArea(4046.86, AreaUnit.ACRE), 0.01)
    }

    @Test
    fun `convertArea square metres to are`() {
        assertEquals(1.0, UnitConverter.convertArea(100.0, AreaUnit.ARE), 0.001)
    }

    @Test
    fun `convertArea square metres to square km`() {
        assertEquals(1.0, UnitConverter.convertArea(1_000_000.0, AreaUnit.SQUARE_KM), 0.001)
    }

    @Test
    fun `convertArea square metres to square foot`() {
        assertEquals(1076.39, UnitConverter.convertArea(100.0, AreaUnit.SQUARE_FOOT), 0.01)
    }

    @Test
    fun `convertArea square metres to square yard`() {
        assertEquals(119.599, UnitConverter.convertArea(100.0, AreaUnit.SQUARE_YARD), 0.01)
    }

    @Test
    fun `convertDistance metres to kilometres`() {
        assertEquals(1.0, UnitConverter.convertDistance(1000.0, DistanceUnit.KILOMETRE), 0.001)
    }

    @Test
    fun `convertDistance metres to miles`() {
        assertEquals(1.0, UnitConverter.convertDistance(1609.34, DistanceUnit.MILE), 0.01)
    }

    @Test
    fun `convertDistance metres to metres`() {
        assertEquals(100.0, UnitConverter.convertDistance(100.0, DistanceUnit.METRE), 0.001)
    }

    @Test
    fun `convertDistance metres to yards`() {
        assertEquals(109.361, UnitConverter.convertDistance(100.0, DistanceUnit.YARD), 0.01)
    }

    @Test
    fun `convertDistance metres to feet`() {
        assertEquals(328.084, UnitConverter.convertDistance(100.0, DistanceUnit.FOOT), 0.01)
    }

    @Test
    fun `formatArea formats with specified decimals`() {
        val formatted = UnitConverter.formatArea(10000.0, AreaUnit.HECTARE, 2)
        assertEquals("1.00 ha", formatted)
    }

    @Test
    fun `formatAllAreaUnits returns all 7 pairs`() {
        val results = UnitConverter.formatAllAreaUnits(10000.0)
        assertEquals(7, results.size)
        assertEquals(AreaUnit.SQUARE_METRE, results[0].first)
        assertEquals(AreaUnit.HECTARE, results[1].first)
        assertEquals(AreaUnit.ARE, results[2].first)
        assertEquals(AreaUnit.ACRE, results[3].first)
        assertEquals(AreaUnit.SQUARE_KM, results[4].first)
        assertEquals(AreaUnit.SQUARE_FOOT, results[5].first)
        assertEquals(AreaUnit.SQUARE_YARD, results[6].first)
    }

    @Test
    fun `formatAllAreaUnits each entry has non-empty formatted value`() {
        val results = UnitConverter.formatAllAreaUnits(10000.0)
        results.forEach { (_, formatted) ->
            assert(formatted.isNotBlank())
            assert(formatted.contains(" "))
        }
    }

    @Test
    fun `convertArea edge case 0 square metres`() {
        assertEquals(0.0, UnitConverter.convertArea(0.0, AreaUnit.HECTARE), 0.0)
        assertEquals(0.0, UnitConverter.convertArea(0.0, AreaUnit.ACRE), 0.0)
        assertEquals(0.0, UnitConverter.convertArea(0.0, AreaUnit.SQUARE_METRE), 0.0)
    }
}
