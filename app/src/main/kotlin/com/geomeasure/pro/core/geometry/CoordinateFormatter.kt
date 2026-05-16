package com.geomeasure.pro.core.geometry

import kotlin.math.*

data class BearingResult(
    val azimuthDeg: Double,
    val azimuthDms: String,
    val distanceM: Double,
    val bearingLabel: String  // e.g. "N 45° E" or "S 30° W"
)

data class AngleResult(
    val interiorDeg: Double,
    val interiorDms: String,
    val turnDirection: String  // "Left" or "Right"
)

data class DmsCoordinate(
    val degrees: Int,
    val minutes: Int,
    val seconds: Double,
    val direction: Char,  // N, S, E, W
    val decimal: Double
) {
    val dms: String get() = "${degrees}°${minutes}'${"%.1f".format(seconds)}\"$direction"
    val dmsCompact: String get() = "${degrees}°${minutes}'${direction}"
}

object CoordinateFormatter {

    fun toDms(decimal: Double, isLatitude: Boolean): DmsCoordinate {
        val direction = when {
            isLatitude -> if (decimal >= 0) 'N' else 'S'
            else -> if (decimal >= 0) 'E' else 'W'
        }
        val abs = abs(decimal)
        val deg = abs.toInt()
        val minRem = (abs - deg) * 60
        val min = minRem.toInt()
        val sec = (minRem - min) * 60
        return DmsCoordinate(deg, min, sec, direction, decimal)
    }

    fun formatDms(decimal: Double, isLatitude: Boolean): String =
        toDms(decimal, isLatitude).dms

    fun formatDmsCompact(decimal: Double, isLatitude: Boolean): String =
        toDms(decimal, isLatitude).dmsCompact

    fun bearingBetween(lat1: Double, lon1: Double, lat2: Double, lon2: Double): BearingResult {
        val dLon = Math.toRadians(lon2 - lon1)
        val rLat1 = Math.toRadians(lat1)
        val rLat2 = Math.toRadians(lat2)
        val x = sin(dLon) * cos(rLat2)
        val y = cos(rLat1) * sin(rLat2) - sin(rLat1) * cos(rLat2) * cos(dLon)
        val azimuthDeg = (Math.toDegrees(atan2(x, y)) + 360) % 360

        val dmsDir = azimuthToDms(azimuthDeg)
        val distance = GeometryCalculator.haversineDistance(GeoPoint(lat1, lon1), GeoPoint(lat2, lon2))
        val bearingLabel = azimuthToBearingLabel(azimuthDeg)
        val azimuthDms = "${azimuthDeg.toInt()}°${((azimuthDeg - azimuthDeg.toInt()) * 60).toInt()}'"

        return BearingResult(azimuthDeg, azimuthDms, distance, bearingLabel)
    }

    private fun azimuthToDms(azimuth: Double): String {
        val deg = azimuth.toInt()
        val min = ((azimuth - deg) * 60).toInt()
        val sec = (((azimuth - deg) * 60 - min) * 60).toInt()
        return "${deg}°${min}'${sec}\""
    }

    private fun azimuthToBearingLabel(azimuth: Double): String {
        val dirs = listOf("N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE",
            "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW")
        val index = ((azimuth + 11.25) / 22.5).toInt() % 16
        return dirs[index]
    }

    fun interiorAngle(
        prevLat: Double, prevLon: Double,
        currLat: Double, currLon: Double,
        nextLat: Double, nextLon: Double
    ): AngleResult {
        val bearingPrevToCurr = bearingBetween(prevLat, prevLon, currLat, currLon).azimuthDeg
        val bearingCurrToNext = bearingBetween(currLat, currLon, nextLat, nextLon).azimuthDeg
        var angle = (bearingCurrToNext - bearingPrevToCurr + 360) % 360
        val turnDir = if (angle > 180) "Left" else "Right"
        if (angle > 180) angle = 360 - angle
        val dms = "${angle.toInt()}°${((angle - angle.toInt()) * 60).toInt()}'"
        return AngleResult(angle, dms, turnDir)
    }

    fun formatArea(areaM2: Double, lang: String = "en"): String {
        return when {
            areaM2 >= 10000 -> "${"%.2f".format(areaM2 / 10000)} ha"
            areaM2 >= 1000 -> "${"%.2f".format(areaM2 / 100)} a"
            else -> "${"%.2f".format(areaM2)} m${if (lang == "id") "\u00B2" else "2"}"
        }
    }

    fun formatDistance(meters: Double, lang: String = "en"): String {
        return when {
            meters >= 1000 -> "${"%.2f".format(meters / 1000)} km"
            else -> "${"%.1f".format(meters)} m"
        }
    }
}
