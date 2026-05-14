package com.geomeasure.pro.presentation.screens.settings

import android.app.Application
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.geomeasure.pro.core.map.OfflineMapManager
import com.geomeasure.pro.core.util.UnitConverter
import com.geomeasure.pro.data.local.db.AppDatabase
import com.geomeasure.pro.data.local.db.dao.ProjectDao
import com.geomeasure.pro.data.local.db.dao.VertexDao
import com.geomeasure.pro.data.local.prefs.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

data class SettingsUiState(
    val isBackingUp: Boolean = false,
    val isRestoring: Boolean = false,
    val isDeleting: Boolean = false,
    val isClearingCache: Boolean = false,
    val message: String? = null,
    val error: String? = null,
    val backupUri: Uri? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    private val prefs: AppPreferences,
    private val db: AppDatabase,
    private val projectDao: ProjectDao,
    private val vertexDao: VertexDao
) : AndroidViewModel(application) {

    val settings = prefs.settings

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch { prefs.setDarkMode(enabled) }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch { prefs.setDynamicColor(enabled) }
    }

    fun setGpsPanel(enabled: Boolean) {
        viewModelScope.launch { prefs.setShowGpsPanel(enabled) }
    }

    fun setAreaUnit(unit: UnitConverter.AreaUnit) {
        viewModelScope.launch { prefs.setDefaultAreaUnit(unit) }
    }

    fun setDistanceUnit(unit: UnitConverter.DistanceUnit) {
        viewModelScope.launch { prefs.setDefaultDistanceUnit(unit) }
    }

    fun setDecimals(places: Int) {
        viewModelScope.launch { prefs.setDecimalPlaces(places) }
    }

    fun setAccuracy(threshold: Float) {
        viewModelScope.launch { prefs.setGpsAccuracyThreshold(threshold) }
    }

    fun setMinDist(distance: Float) {
        viewModelScope.launch { prefs.setGpsMinDistance(distance) }
    }

    fun setInterval(interval: Long) {
        viewModelScope.launch { prefs.setGpsIntervalSec(interval) }
    }

    fun setTileSource(source: String) {
        viewModelScope.launch { prefs.setTileSource(source) }
    }

    fun setCacheSize(size: Int) {
        viewModelScope.launch { prefs.setCacheSizeMb(size) }
    }

    fun exportBackup(uri: Uri? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.update { it.copy(message = "Exporting backup...") }
                val ctx = getApplication<Application>()
                val dbFile = ctx.getDatabasePath("geomeasure.db")
                if (!dbFile.exists()) {
                    _uiState.update { it.copy(error = "No database found") }
                    return@launch
                }
                val backupFile = File(ctx.cacheDir, "geomeasure_backup.db")
                dbFile.inputStream().use { input ->
                    backupFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                val uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", backupFile)
                _uiState.update { it.copy(message = "Backup ready at $uri", backupUri = uri) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Backup failed: ${e.message}") }
            }
        }
    }

    fun importBackup(uri: Uri? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRestoring = true, error = null, message = null) }
            try {
                withContext(Dispatchers.IO) {
                    val ctx = getApplication<Application>()
                    val dbFile = ctx.getDatabasePath("geomeasure.db")
                    val walFile = File(dbFile.parent, "geomeasure.db-wal")
                    val shmFile = File(dbFile.parent, "geomeasure.db-shm")

                    if (uri != null) {
                        ctx.contentResolver.openInputStream(uri)?.use { input ->
                            dbFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                        walFile.delete()
                        shmFile.delete()
                    } else {
                        val exportDir = File(ctx.cacheDir, "backups")
                        val backupFile = File(exportDir, "GeoMeasure_backup.gmbackup")
                        if (!backupFile.exists()) throw IllegalStateException("No backup file found")
                        backupFile.inputStream().use { input ->
                            dbFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                        walFile.delete()
                        shmFile.delete()
                    }

                    _uiState.update {
                        it.copy(isRestoring = false, message = "Backup restored successfully")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isRestoring = false, error = "Restore failed: ${e.message}")
                }
            }
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, error = null, message = null) }
            try {
                withContext(Dispatchers.IO) {
                    val projectList = projectDao.getAllProjects().first()
                    for (project in projectList) {
                        vertexDao.deleteVerticesForProject(project.id)
                        projectDao.deleteProject(project)
                    }
                    android.util.Log.d("SettingsVM", "All data deleted")
                }
                _uiState.update {
                    it.copy(isDeleting = false, message = "All data deleted successfully")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isDeleting = false, error = "Delete failed: ${e.message}")
                }
            }
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            _uiState.update { it.copy(isClearingCache = true, error = null, message = null) }
            try {
                withContext(Dispatchers.IO) {
                    val manager = OfflineMapManager(getApplication())
                    manager.clearCache()
                }
                _uiState.update {
                    it.copy(isClearingCache = false, message = "Tile cache cleared")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isClearingCache = false, error = "Cache clear failed: ${e.message}")
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(message = null, error = null) }
    }
}
