package com.geomeasure.pro.presentation.screens.map

import com.geomeasure.pro.data.local.db.entities.ProjectEntity
import com.geomeasure.pro.data.local.db.entities.VertexEntity
import com.geomeasure.pro.domain.model.GpsStatus

data class MapUiState(
    val currentProject: ProjectEntity? = null,
    val vertices: List<VertexEntity> = emptyList(),
    val isRecording: Boolean = false,
    val measurementType: MeasurementType = MeasurementType.TAP,
    val gpsStatus: GpsStatus = GpsStatus(),
    val areaM2: Double = 0.0,
    val perimeterM: Double = 0.0,
    val showBottomSheet: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val currentTool: MeasurementType get() = measurementType
}

enum class MeasurementType {
    TAP, WALK, LINE, POINT
}
