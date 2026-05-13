package com.geomeasure.pro.domain.model

data class Project(
    val id: String = "",
    val name: String = "",
    val notes: String = "",
    val folderId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis(),
    val measurementType: String = "POLYGON",
    val areaM2: Double = 0.0,
    val perimeterM: Double = 0.0,
    val vertexCount: Int = 0
)
