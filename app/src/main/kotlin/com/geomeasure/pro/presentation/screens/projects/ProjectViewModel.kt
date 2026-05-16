package com.geomeasure.pro.presentation.screens.projects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geomeasure.pro.data.local.db.entities.ProjectEntity
import com.geomeasure.pro.domain.repository.MeasurementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProjectListUiState(
    val projects: List<ProjectEntity> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class ProjectViewModel @Inject constructor(
    private val repository: MeasurementRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProjectListUiState())
    val uiState: StateFlow<ProjectListUiState> = _uiState.asStateFlow()
    private var collectionJob: Job? = null

    init {
        loadProjects()
    }

    private fun loadProjects() {
        collectionJob?.cancel()
        collectionJob = viewModelScope.launch {
            try {
                repository.getAllProjects().collect { projects ->
                    _uiState.update {
                        it.copy(projects = projects, isLoading = false)
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Failed to load projects: ${e.message}") }
            }
        }
    }

    fun search(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        collectionJob?.cancel()
        collectionJob = viewModelScope.launch {
            try {
                if (query.isBlank()) {
                    repository.getAllProjects().collect { projects ->
                        _uiState.update { it.copy(projects = projects) }
                    }
                } else {
                    repository.searchProjects(query).collect { projects ->
                        _uiState.update { it.copy(projects = projects) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Search failed: ${e.message}") }
            }
        }
    }

    fun deleteProject(project: ProjectEntity) {
        viewModelScope.launch {
            try {
                repository.deleteProject(project)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to delete project: ${e.message}") }
            }
        }
    }
}
