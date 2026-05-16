package com.geomeasure.pro.presentation.screens.tools

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.geomeasure.pro.data.local.db.AppDatabase
import com.geomeasure.pro.data.local.db.entities.ProjectEntity
import com.geomeasure.pro.data.local.db.entities.VertexEntity
import com.geomeasure.pro.data.export.ShapefileExporter
import com.geomeasure.pro.domain.usecase.ExportProjectUseCase
import com.geomeasure.pro.domain.usecase.ImportFileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

data class ExportUiState(
    val projects: List<ProjectEntity> = emptyList(),
    val selectedProject: ProjectEntity? = null,
    val vertices: List<VertexEntity> = emptyList(),
    val isExporting: Boolean = false,
    val exportFormat: String? = null,
    val exportedContent: String? = null,
    val exportedFile: Uri? = null,
    val error: String? = null,
    val successMessage: String? = null
)

enum class ExportFormat {
    GEOJSON, KML, GPX, CSV, PDF
}

@HiltViewModel
class ExportViewModel @Inject constructor(
    application: Application,
    private val exportProject: ExportProjectUseCase,
    private val importFile: ImportFileUseCase,
    private val shapefileExporter: ShapefileExporter,
    private val db: AppDatabase
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ExportUiState())
    val uiState: StateFlow<ExportUiState> = _uiState.asStateFlow()

    init {
        loadProjects()
    }

    private fun loadProjects() {
        viewModelScope.launch {
            db.projectDao().getAllProjects().collect { projects ->
                _uiState.update { it.copy(projects = projects) }
            }
        }
    }

    fun selectProject(project: ProjectEntity) {
        viewModelScope.launch {
            val vertices = db.vertexDao().getVerticesForProject(project.id)
            _uiState.update { it.copy(selectedProject = project, vertices = vertices) }
        }
    }

    fun exportGeoJson() {
        exportFormat(ExportFormat.GEOJSON)
    }

    fun exportKml() {
        exportFormat(ExportFormat.KML)
    }

    fun exportGpx() {
        exportFormat(ExportFormat.GPX)
    }

    fun exportCsv() {
        exportFormat(ExportFormat.CSV)
    }

    fun exportPdf() {
        exportFormat(ExportFormat.PDF)
    }

    fun exportSHP(projectId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val project = db.projectDao().getProjectById(projectId) ?: return@launch
                val vertices = db.vertexDao().getVerticesForProject(projectId)
                val ctx = getApplication<Application>()
                val (shpFile, _, _) = shapefileExporter.export(project, vertices, ctx)
                _uiState.update {
                    it.copy(
                        exportedFile = androidx.core.content.FileProvider.getUriForFile(
                            ctx,
                            "${ctx.packageName}.fileprovider",
                            shpFile
                        ),
                        successMessage = "SHP exported: ${shpFile.name}"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "SHP export failed: ${e.message}") }
            }
        }
    }

    private fun exportFormat(format: ExportFormat) {
        val project = _uiState.value.selectedProject ?: return
        val vertices = _uiState.value.vertices
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, error = null, exportFormat = format.name) }
            try {
                when (format) {
                    ExportFormat.GEOJSON -> {
                        val content = exportProject.exportGeoJson(project, vertices)
                        createShareFile(content, "geojson")
                    }
                    ExportFormat.KML -> {
                        val content = exportProject.exportKml(project, vertices)
                        createShareFile(content, "kml")
                    }
                    ExportFormat.GPX -> {
                        val content = exportProject.exportGpx(project, vertices)
                        createShareFile(content, "gpx")
                    }
                    ExportFormat.CSV -> {
                        val content = exportProject.exportCsv(project, vertices)
                        createShareFile(content, "csv")
                    }
                    ExportFormat.PDF -> {
                        val file = exportProject.exportPdf(project, vertices, null)
                        val uri = androidx.core.content.FileProvider.getUriForFile(
                            getApplication(),
                            "${getApplication<Application>().packageName}.fileprovider",
                            file
                        )
                        _uiState.update {
                            it.copy(
                                isExporting = false,
                                exportedFile = uri,
                                successMessage = "PDF exported: ${file.name}"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isExporting = false, error = "Export failed: ${e.message}") }
            }
        }
    }

    private fun createShareFile(content: String, extension: String) {
        val safeName = (_uiState.value.selectedProject?.name ?: "export").replace(Regex("[/\\\\?%*:|\"<>]"), "_")
        val fileName = "${safeName}.$extension"
        val file = java.io.File(getApplication<Application>().cacheDir, fileName)
        file.parentFile?.mkdirs()
        file.writeText(content)
        val uri = androidx.core.content.FileProvider.getUriForFile(
            getApplication(),
            "${getApplication<Application>().packageName}.fileprovider",
            file
        )
        _uiState.update {
            it.copy(isExporting = false, exportedContent = content, exportedFile = uri)
        }
    }

    fun importFile(uri: Uri) {
        viewModelScope.launch {
            try {
                importFile(uri, getApplication())
                _uiState.update {
                    it.copy(successMessage = "File imported successfully", error = null)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Import failed: ${e.message}") }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}
