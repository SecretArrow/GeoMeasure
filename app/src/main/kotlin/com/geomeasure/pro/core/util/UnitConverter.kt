package com.geomeasure.pro.core.util

object UnitConverter {

    enum class AreaUnit(val symbol: String, val factor: Double) {
        SQUARE_METRE("m2", 1.0),
        HECTARE("ha", 1e-4),
        ARE("a", 1e-2),
        ACRE("ac", 2.47105e-4),
        SQUARE_KM("km2", 1e-6),
        SQUARE_FOOT("ft2", 10.7639),
        SQUARE_YARD("yd2", 1.19599)
    }

    enum class DistanceUnit(val symbol: String, val factor: Double) {
        METRE("m", 1.0),
        KILOMETRE("km", 1e-3),
        MILE("mi", 6.21371e-4),
        YARD("yd", 1.09361),
        FOOT("ft", 3.28084)
    }

    fun convertArea(squareMetres: Double, to: AreaUnit): Double =
        squareMetres * to.factor

    fun convertDistance(metres: Double, to: DistanceUnit): Double =
        metres * to.factor

    fun formatArea(squareMetres: Double, unit: AreaUnit, decimals: Int = 4): String =
        "%.${decimals}f ${unit.symbol}".format(convertArea(squareMetres, unit))

    fun formatAllAreaUnits(squareMetres: Double, decimals: Int = 3): List<Pair<AreaUnit, String>> =
        AreaUnit.values().map { unit ->
            unit to formatArea(squareMetres, unit, decimals)
        }
}
