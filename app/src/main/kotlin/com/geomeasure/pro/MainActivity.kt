package com.geomeasure.pro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.geomeasure.pro.data.local.prefs.AppSettings
import com.geomeasure.pro.presentation.navigation.AppNavigation
import com.geomeasure.pro.presentation.screens.settings.SettingsViewModel
import com.geomeasure.pro.presentation.theme.GeoMeasureTheme
import dagger.hilt.android.AndroidEntryPoint
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import java.io.File

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().apply {
            userAgentValue = BuildConfig.APPLICATION_ID
            osmdroidTileCache = File(cacheDir, "osm_tiles")
        }

        enableEdgeToEdge()

        setContent {
            val settingsVm: SettingsViewModel = hiltViewModel()
            val settings by settingsVm.settings.collectAsState(initial = AppSettings())

            GeoMeasureTheme(
                darkTheme = settings.darkMode,
                dynamicColor = settings.dynamicColor
            ) {
                AppNavigation()
            }
        }
    }
}
