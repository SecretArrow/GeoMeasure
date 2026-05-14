package com.geomeasure.pro.core.geometry

import kotlin.math.*

/**
 * WGS-84 to UTM / TM-3 projection utilities for Indonesian survey standards.
 * TM-3 (Transverse Mercator 3°) is used by BPN Indonesia for local mapping.
 *
 * Based on standard formulas:
 *   - UTM Zone = floor((lon + 180) / 6) + 1
 *   - Transverse Mercator projection using WGS-84 ellipsoid parameters
 */
object UtmProjection {

    // WGS-84 ellipsoid constants
    private const val A = 6378137.0          // semi-major axis (meters)
    private const val F = 1.0 / 298.257223563 // flattening
    private const val E2 = 2 * F - F * F     // first eccentricity squared
    private const val E4 = E2 * E2
    private const val E6 = E4 * E2

    /**
     * UTM zone number for a given longitude.
     * Indonesia spans zones 46-54 (longitude 94°E to 142°E).
     */
    fun utmZone(lon: Double): Int = floor((lon + 180.0) / 6.0).toInt() + 1

    /**
     * Central meridian for a UTM zone.
     */
    fun centralMeridian(zone: Int): Double = (zone - 1) * 6.0 - 180 + 3

    /**
     * Convert WGS-84 latitude/longitude to UTM easting/northing.
     *
     * @param lat Latitude in degrees
     * @param lon Longitude in degrees
     * @param zone Optional UTM zone (auto-calculated from longitude if not provided)
     * @return Triple(easting, northing, zone)
     */
    fun toUtm(lat: Double, lon: Double, zone: Int = utmZone(lon)): Triple<Double, Double, Int> {
        val latRad = Math.toRadians(lat)
        val lonRad = Math.toRadians(lon)
        val lon0 = Math.toRadians(centralMeridian(zone))
        val dLon = lonRad - lon0

        val sinLat = sin(latRad)
        val cosLat = cos(latRad)
        val tanLat = tan(latRad)

        val N = A / sqrt(1.0 - E2 * sinLat * sinLat)
        val T = tanLat * tanLat
        val C = E2 / (1.0 - E2) * cosLat * cosLat
        val A1 = cosLat * dLon

        // Meridional arc
        val M0 = 1.0 - E2 / 4.0 - 3.0 * E4 / 64.0 - 5.0 * E6 / 256.0
        val M2 = 3.0 * E2 / 8.0 + 3.0 * E4 / 32.0 + 45.0 * E6 / 1024.0
        val M4 = 15.0 * E4 / 256.0 + 45.0 * E6 / 1024.0
        val M6 = 35.0 * E6 / 3072.0
        val M = A * (M0 * latRad - M2 * sin(2 * latRad) + M4 * sin(4 * latRad) - M6 * sin(6 * latRad))

        val easting = 500000.0 + N * (A1 + (1.0 - T + C) * A1.pow(3) / 6.0
                + (5.0 - 18.0 * T + T * T + 72.0 * C - 58.0 * E2 / (1.0 - E2)) * A1.pow(5) / 120.0)

        val northing = M + N * tanLat * (A1 * A1 / 2.0
                + (5.0 - T + 9.0 * C + 4.0 * C * C) * A1.pow(4) / 24.0
                + (61.0 - 58.0 * T + T * T + 600.0 * C - 330.0 * E2 / (1.0 - E2)) * A1.pow(6) / 720.0)

        val hemi = if (lat < 0) "S" else "N"
        val finalNorthing = if (lat < 0) northing + 10000000.0 else northing

        return Triple(easting, finalNorthing, zone)
    }

    /**
     * Get UTM string representation.
     */
    fun utmString(lat: Double, lon: Double): String {
        val (easting, northing, zone) = toUtm(lat, lon)
        val hemi = if (lat < 0) "S" else "N"
        return "${zone}${hemi} ${"%.2f".format(easting)}E ${"%.2f".format(northing)}N"
    }

    /**
     * TM-3 (Transverse Mercator 3°) — used by BPN Indonesia.
     * Zone = floor((lon - 93) / 3) + 1 (for Indonesia: longitude 94-142)
     */
    fun tm3Zone(lon: Double): Int {
        if (lon < 94 || lon > 142) return 0 // outside Indonesia
        return floor((lon - 93.0) / 3.0).toInt() + 1
    }

    fun tm3CentralMeridian(zone: Int): Double = 93.0 + zone * 3.0 - 1.5

    /**
     * Convert WGS-84 to TM-3 (Indonesia local projection).
     */
    fun toTm3(lat: Double, lon: Double): Triple<Double, Double, Int> {
        val zone = tm3Zone(lon)
        if (zone == 0) return toUtm(lat, lon) // fallback to UTM if outside Indonesia

        val latRad = Math.toRadians(lat)
        val lonRad = Math.toRadians(lon)
        val lon0 = Math.toRadians(tm3CentralMeridian(zone))
        val dLon = lonRad - lon0

        val sinLat = sin(latRad)
        val cosLat = cos(latRad)
        val tanLat = tan(latRad)

        val N = A / sqrt(1.0 - E2 * sinLat * sinLat)
        val T = tanLat * tanLat
        val C = E2 / (1.0 - E2) * cosLat * cosLat
        val A1 = cosLat * dLon

        val M0 = 1.0 - E2 / 4.0 - 3.0 * E4 / 64.0 - 5.0 * E6 / 256.0
        val M2 = 3.0 * E2 / 8.0 + 3.0 * E4 / 32.0 + 45.0 * E6 / 1024.0
        val M4 = 15.0 * E4 / 256.0 + 45.0 * E6 / 1024.0
        val M6 = 35.0 * E6 / 3072.0
        val M = A * (M0 * latRad - M2 * sin(2 * latRad) + M4 * sin(4 * latRad) - M6 * sin(6 * latRad))

        val easting = 200000.0 + N * (A1 + (1.0 - T + C) * A1.pow(3) / 6.0
                + (5.0 - 18.0 * T + T * T + 72.0 * C - 58.0 * E2 / (1.0 - E2)) * A1.pow(5) / 120.0)

        val northing = M + N * tanLat * (A1 * A1 / 2.0
                + (5.0 - T + 9.0 * C + 4.0 * C * C) * A1.pow(4) / 24.0
                + (61.0 - 58.0 * T + T * T + 600.0 * C - 330.0 * E2 / (1.0 - E2)) * A1.pow(6) / 720.0)

        val finalNorthing = if (lat < 0) northing + 10000000.0 else northing

        return Triple(easting, finalNorthing, zone)
    }

    fun tm3String(lat: Double, lon: Double): String {
        val (easting, northing, zone) = toTm3(lat, lon)
        return "TM3-${zone} ${"%.2f".format(easting)}E ${"%.2f".format(northing)}N"
    }
}
