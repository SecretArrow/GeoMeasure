package com.geomeasure.pro.domain.model

data class Vertex(
    val id: String = "",
    val projectId: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val altitude: Double = 0.0,
    val order: Int = 0,
    val gpsAccuracy: Float? = null,
    val timestamp: Long = System.currentTimeMillis()
)
