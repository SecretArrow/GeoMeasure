package com.geomeasure.pro.presentation.screens.projects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geomeasure.pro.data.local.db.entities.ProjectEntity
import com.geomeasure.pro.domain.repository.MeasurementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProjectListUiState(
    val projects: List<ProjectEntity> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true
)

@HiltViewModel
class ProjectViewModel @Inject constructor(
    private val repository: MeasurementRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProjectListUiState())
    val uiState: StateFlow<ProjectListUiState> = _uiState.asStateFlow()

    init {
        loadProjects()
    }

    private fun loadProjects() {
        viewModelScope.launch {
            repository.getAllProjects().collect { projects ->
                _uiState.update {
                    it.copy(projects = projects, isLoading = false)
                }
            }
        }
    }

    fun search(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        viewModelScope.launch {
            if (query.isBlank()) {
                loadProjects()
            } else {
                repository.searchProjects(query).collect { projects ->
                    _uiState.update { it.copy(projects = projects) }
                }
            }
        }
    }

    fun deleteProject(project: ProjectEntity) {
        viewModelScope.launch {
            repository.deleteProject(project)
        }
    }
}
