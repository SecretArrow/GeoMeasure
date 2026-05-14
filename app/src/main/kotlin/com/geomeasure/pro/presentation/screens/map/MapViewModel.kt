package com.geomeasure.pro.presentation.screens.map

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.geomeasure.pro.data.local.db.entities.ProjectEntity
import com.geomeasure.pro.data.local.db.entities.VertexEntity
import com.geomeasure.pro.domain.model.GpsStatus
import com.geomeasure.pro.domain.repository.MeasurementRepository
import com.geomeasure.pro.domain.usecase.CalculateAreaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    application: Application,
    private val repository: MeasurementRepository,
    private val calculateArea: CalculateAreaUseCase
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private val undoStack = mutableListOf<List<VertexEntity>>()
    private val redoStack = mutableListOf<List<VertexEntity>>()

    private fun saveUndoState() {
        undoStack.add(_uiState.value.vertices.toList())
        redoStack.clear()
    }

    fun undo() {
        val current = _uiState.value.vertices
        if (undoStack.isNotEmpty()) {
            redoStack.add(current.toList())
            val previous = undoStack.removeLast()
            val (area, perimeter) = calculateArea(previous)
            _uiState.update { it.copy(vertices = previous, areaM2 = area, perimeterM = perimeter) }
        }
    }

    fun redo() {
        val current = _uiState.value.vertices
        if (redoStack.isNotEmpty()) {
            undoStack.add(current.toList())
            val next = redoStack.removeLast()
            val (area, perimeter) = calculateArea(next)
            _uiState.update { it.copy(vertices = next, areaM2 = area, perimeterM = perimeter) }
        }
    }

    fun createNewProject(name: String = "New Measurement") {
        undoStack.clear()
        redoStack.clear()
        viewModelScope.launch {
            val project = ProjectEntity(
                id = UUID.randomUUID().toString(),
                name = name
            )
            repository.insertProject(project)
            _uiState.update {
                it.copy(
                    currentProject = project,
                    vertices = emptyList(),
                    areaM2 = 0.0,
                    perimeterM = 0.0
                )
            }
        }
    }

    fun loadProject(projectId: String) {
        undoStack.clear()
        redoStack.clear()
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val project = repository.getProjectById(projectId)
            if (project != null) {
                repository.getVerticesForProject(projectId).collect { vertices ->
                    val (area, perimeter) = calculateArea(vertices)
                    _uiState.update {
                        it.copy(
                            currentProject = project,
                            vertices = vertices,
                            areaM2 = area,
                            perimeterM = perimeter,
                            isLoading = false
                        )
                    }
                }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Project not found") }
            }
        }
    }

    fun addVertex(latitude: Double, longitude: Double) {
        val project = _uiState.value.currentProject ?: return
        saveUndoState()
        viewModelScope.launch {
            val vertex = VertexEntity(
                projectId = project.id,
                latitude = latitude,
                longitude = longitude,
                order = _uiState.value.vertices.size
            )
            repository.insertVertex(vertex)
        }
    }

    fun removeVertex(vertex: VertexEntity) {
        saveUndoState()
        viewModelScope.launch {
            repository.deleteVertex(vertex)
        }
    }

    fun updateVertex(vertex: VertexEntity) {
        saveUndoState()
        viewModelScope.launch {
            repository.updateVertex(vertex)
        }
    }

    fun setRecording(recording: Boolean) {
        _uiState.update { it.copy(isRecording = recording) }
    }

    fun setMeasurementType(type: MeasurementType) {
        _uiState.update { it.copy(measurementType = type) }
    }

    fun updateGpsStatus(status: GpsStatus) {
        _uiState.update { it.copy(gpsStatus = status) }
    }

    fun toggleBottomSheet() {
        _uiState.update { it.copy(showBottomSheet = !it.showBottomSheet) }
    }

    fun saveProject() {
        val project = _uiState.value.currentProject ?: return
        val (area, perimeter) = calculateArea(_uiState.value.vertices)
        viewModelScope.launch {
            repository.updateProject(
                project.copy(
                    areaM2 = area,
                    perimeterM = perimeter,
                    modifiedAt = System.currentTimeMillis()
                )
            )
            _uiState.update { it.copy(areaM2 = area, perimeterM = perimeter) }
        }
    }
}
