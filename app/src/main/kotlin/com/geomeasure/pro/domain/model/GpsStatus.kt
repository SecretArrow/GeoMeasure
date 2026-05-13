package com.geomeasure.pro.domain.model

data class GpsStatus(
    val accuracyM: Float = 0f,
    val satellitesUsed: Int = 0,
    val satellitesInView: Int = 0,
    val hdop: Float = 0f,
    val altitudeM: Double = 0.0
)
