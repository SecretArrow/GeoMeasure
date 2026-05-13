package com.geomeasure.pro.presentation.screens.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.geomeasure.pro.core.util.UnitConverter
import com.geomeasure.pro.data.local.prefs.AppSettings

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val settings by viewModel.settings.collectAsState(initial = AppSettings())
    val uiState by viewModel.uiState.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        uri?.let { viewModel.exportBackup(it) }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importBackup(it) }
    }

    LaunchedEffect(uiState.message) {
        uiState.message?.let { viewModel.clearMessages() }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete All Data") },
            text = { Text("This will permanently delete all projects, measurements, and settings. This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteAll()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Settings", style = MaterialTheme.typography.headlineMedium)
        }

        item {
            SettingsGroup("Units") {
                UnitDropdown(
                    label = "Default Area Unit",
                    current = settings.defaultAreaUnit,
                    items = UnitConverter.AreaUnit.values().toList(),
                    onSelect = { viewModel.setAreaUnit(it) }
                )
                UnitDropdown(
                    label = "Default Distance Unit",
                    current = settings.defaultDistanceUnit,
                    items = UnitConverter.DistanceUnit.values().toList(),
                    onSelect = { viewModel.setDistanceUnit(it) }
                )
                SliderSetting(
                    label = "Decimal Places (0-8)",
                    value = settings.decimalPlaces.toFloat(),
                    range = 0f..8f,
                    onValueChange = { viewModel.setDecimals(it.toInt()) }
                )
            }
        }

        item {
            SettingsGroup("GPS") {
                SliderSetting(
                    label = "Accuracy Threshold (m)",
                    value = settings.gpsAccuracyThreshold,
                    range = 3f..50f,
                    onValueChange = { viewModel.setAccuracy(it) }
                )
                SliderSetting(
                    label = "Min Distance (m)",
                    value = settings.gpsMinDistance,
                    range = 0.5f..20f,
                    onValueChange = { viewModel.setMinDist(it) }
                )
                SliderSetting(
                    label = "Update Interval (s)",
                    value = settings.gpsIntervalSec.toFloat(),
                    range = 1f..30f,
                    onValueChange = { viewModel.setInterval(it.toLong()) }
                )
            }
        }

        item {
            SettingsGroup("Map") {
                SliderSetting(
                    label = "Cache Size (MB)",
                    value = settings.cacheSizeMb.toFloat(),
                    range = 50f..2048f,
                    onValueChange = { viewModel.setCacheSize(it.toInt()) }
                )
                OutlinedButton(
                    onClick = { viewModel.clearCache() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Clear Map Cache")
                }
            }
        }

        item {
            SettingsGroup("Appearance") {
                SwitchSetting(
                    label = "Dark Mode",
                    checked = settings.darkMode,
                    onCheckedChange = { viewModel.setDarkMode(it) }
                )
                SwitchSetting(
                    label = "Dynamic Colors",
                    checked = settings.dynamicColor,
                    onCheckedChange = { viewModel.setDynamicColor(it) }
                )
                SwitchSetting(
                    label = "Show GPS Panel",
                    checked = settings.showGpsPanel,
                    onCheckedChange = { viewModel.setGpsPanel(it) }
                )
            }
        }

        item {
            SettingsGroup("Data & Privacy") {
                Button(
                    onClick = { exportLauncher.launch("GeoMeasure_backup.gmbackup") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isBackingUp
                ) {
                    Text(if (uiState.isBackingUp) "Exporting..." else "Export Encrypted Backup")
                }
                OutlinedButton(
                    onClick = { importLauncher.launch(arrayOf("application/octet-stream", "*/*")) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isRestoring
                ) {
                    Text(if (uiState.isRestoring) "Restoring..." else "Restore from Backup")
                }
                Button(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete All Data")
                }
            }
        }
    }
}

@Composable
fun SettingsGroup(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
fun SwitchSetting(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun SliderSetting(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = range,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                if (value == value.toInt().toFloat()) value.toInt().toString()
                else "%.1f".format(value),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.width(40.dp)
            )
        }
    }
}

@Composable
fun <T> UnitDropdown(
    label: String,
    current: T,
    items: List<T>,
    onSelect: (T) -> Unit
) where T : Enum<T> {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                (current as? UnitConverter.AreaUnit)?.symbol
                    ?: (current as? UnitConverter.DistanceUnit)?.symbol
                    ?: current.toString()
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Text(
                            (item as? UnitConverter.AreaUnit)?.symbol
                                ?: (item as? UnitConverter.DistanceUnit)?.symbol
                                ?: item.toString()
                        )
                    },
                    onClick = {
                        onSelect(item)
                        expanded = false
                    }
                )
            }
        }
    }
}
