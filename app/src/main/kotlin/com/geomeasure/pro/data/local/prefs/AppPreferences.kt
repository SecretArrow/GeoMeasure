package com.geomeasure.pro.data.local.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.geomeasure.pro.core.util.UnitConverter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

data class AppSettings(
    val darkMode: Boolean = false,
    val dynamicColor: Boolean = true,
    val showGpsPanel: Boolean = true,
    val language: String = "en",
    val coordFormat: String = "dd",
    val defaultAreaUnit: UnitConverter.AreaUnit = UnitConverter.AreaUnit.SQUARE_METRE,
    val defaultDistanceUnit: UnitConverter.DistanceUnit = UnitConverter.DistanceUnit.METRE,
    val decimalPlaces: Int = 4,
    val gpsAccuracyThreshold: Float = 10f,
    val gpsMinDistance: Float = 1f,
    val gpsIntervalMs: Long = 1000L,
    val gpsIntervalSec: Long = 1L,
    val tileSource: String = "MAPNIK",
    val cacheSizeMb: Int = 256
)

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val SHOW_GPS_PANEL = booleanPreferencesKey("show_gps_panel")
        val DEFAULT_AREA_UNIT = stringPreferencesKey("default_area_unit")
        val DEFAULT_DISTANCE_UNIT = stringPreferencesKey("default_distance_unit")
        val DECIMAL_PLACES = intPreferencesKey("decimal_places")
        val GPS_ACCURACY_THRESHOLD = floatPreferencesKey("gps_accuracy_threshold")
        val GPS_MIN_DISTANCE = floatPreferencesKey("gps_min_distance")
        val GPS_INTERVAL_SEC = intPreferencesKey("gps_interval_sec")
        val TILE_SOURCE = stringPreferencesKey("tile_source")
        val CACHE_SIZE_MB = intPreferencesKey("cache_size_mb")
        val LANGUAGE = stringPreferencesKey("language")
        val COORD_FORMAT = stringPreferencesKey("coord_format")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            darkMode = prefs[Keys.DARK_MODE] ?: false,
            dynamicColor = prefs[Keys.DYNAMIC_COLOR] ?: true,
            showGpsPanel = prefs[Keys.SHOW_GPS_PANEL] ?: true,
            defaultAreaUnit = try {
                UnitConverter.AreaUnit.valueOf(prefs[Keys.DEFAULT_AREA_UNIT] ?: "SQUARE_METRE")
            } catch (_: Exception) { UnitConverter.AreaUnit.SQUARE_METRE },
            defaultDistanceUnit = try {
                UnitConverter.DistanceUnit.valueOf(prefs[Keys.DEFAULT_DISTANCE_UNIT] ?: "METRE")
            } catch (_: Exception) { UnitConverter.DistanceUnit.METRE },
            decimalPlaces = prefs[Keys.DECIMAL_PLACES] ?: 4,
            gpsAccuracyThreshold = prefs[Keys.GPS_ACCURACY_THRESHOLD] ?: 10f,
            gpsMinDistance = prefs[Keys.GPS_MIN_DISTANCE] ?: 1f,
            gpsIntervalSec = (prefs[Keys.GPS_INTERVAL_SEC] ?: 1).toLong(),
            tileSource = prefs[Keys.TILE_SOURCE] ?: "MAPNIK",
            cacheSizeMb = prefs[Keys.CACHE_SIZE_MB] ?: 256,
            language = prefs[Keys.LANGUAGE] ?: "en",
            coordFormat = prefs[Keys.COORD_FORMAT] ?: "dd"
        )
    }

    fun getSettingsBlocking(): AppSettings = runBlocking(Dispatchers.IO) {
        context.dataStore.data.map { prefs ->
            AppSettings(
                darkMode = prefs[Keys.DARK_MODE] ?: false,
                dynamicColor = prefs[Keys.DYNAMIC_COLOR] ?: true,
                showGpsPanel = prefs[Keys.SHOW_GPS_PANEL] ?: true,
                defaultAreaUnit = try {
                    UnitConverter.AreaUnit.valueOf(prefs[Keys.DEFAULT_AREA_UNIT] ?: "SQUARE_METRE")
                } catch (_: Exception) { UnitConverter.AreaUnit.SQUARE_METRE },
                defaultDistanceUnit = try {
                    UnitConverter.DistanceUnit.valueOf(prefs[Keys.DEFAULT_DISTANCE_UNIT] ?: "METRE")
                } catch (_: Exception) { UnitConverter.DistanceUnit.METRE },
                decimalPlaces = prefs[Keys.DECIMAL_PLACES] ?: 4,
                gpsAccuracyThreshold = prefs[Keys.GPS_ACCURACY_THRESHOLD] ?: 10f,
                gpsMinDistance = prefs[Keys.GPS_MIN_DISTANCE] ?: 1f,
                gpsIntervalMs = (prefs[Keys.GPS_INTERVAL_SEC] ?: 1) * 1000L,
                gpsIntervalSec = (prefs[Keys.GPS_INTERVAL_SEC] ?: 1).toLong(),
                tileSource = prefs[Keys.TILE_SOURCE] ?: "MAPNIK",
                cacheSizeMb = prefs[Keys.CACHE_SIZE_MB] ?: 256,
            language = prefs[Keys.LANGUAGE] ?: "en",
            coordFormat = prefs[Keys.COORD_FORMAT] ?: "dd"
            )
        }.first()
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DARK_MODE] = enabled }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DYNAMIC_COLOR] = enabled }
    }

    suspend fun setShowGpsPanel(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SHOW_GPS_PANEL] = enabled }
    }

    suspend fun setDefaultAreaUnit(unit: UnitConverter.AreaUnit) {
        context.dataStore.edit { it[Keys.DEFAULT_AREA_UNIT] = unit.name }
    }

    suspend fun setDefaultDistanceUnit(unit: UnitConverter.DistanceUnit) {
        context.dataStore.edit { it[Keys.DEFAULT_DISTANCE_UNIT] = unit.name }
    }

    suspend fun setDecimalPlaces(places: Int) {
        context.dataStore.edit { it[Keys.DECIMAL_PLACES] = places }
    }

    suspend fun setGpsAccuracyThreshold(threshold: Float) {
        context.dataStore.edit { it[Keys.GPS_ACCURACY_THRESHOLD] = threshold }
    }

    suspend fun setGpsMinDistance(distance: Float) {
        context.dataStore.edit { it[Keys.GPS_MIN_DISTANCE] = distance }
    }

    suspend fun setGpsIntervalSec(interval: Long) {
        context.dataStore.edit { it[Keys.GPS_INTERVAL_SEC] = interval.toInt() }
    }

    suspend fun setTileSource(source: String) {
        context.dataStore.edit { it[Keys.TILE_SOURCE] = source }
    }

    suspend fun setCacheSizeMb(size: Int) {
        context.dataStore.edit { it[Keys.CACHE_SIZE_MB] = size }
    }

    suspend fun setLanguage(code: String) {
        context.dataStore.edit { it[Keys.LANGUAGE] = code }
    }

    suspend fun setCoordFormat(format: String) {
        context.dataStore.edit { it[Keys.COORD_FORMAT] = format }
    }
}
