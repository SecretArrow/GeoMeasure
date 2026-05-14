package com.geomeasure.pro.presentation.screens.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
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
            icon = {
                Icon(
                    Icons.Filled.DeleteForever,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(28.dp)
                )
            },
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
                    Icon(Icons.Filled.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
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
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Spacer(Modifier.height(8.dp))
            Text("Settings", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                "Configure your measurement preferences",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            SettingsCard(
                title = "Units",
                subtitle = "Measurement units and precision",
                icon = Icons.Filled.Straighten
            ) {
                UnitDropdown(
                    label = "Default Area Unit",
                    current = settings.defaultAreaUnit,
                    items = UnitConverter.AreaUnit.values().toList(),
                    onSelect = { viewModel.setAreaUnit(it) }
                )
                Spacer(Modifier.height(12.dp))
                UnitDropdown(
                    label = "Default Distance Unit",
                    current = settings.defaultDistanceUnit,
                    items = UnitConverter.DistanceUnit.values().toList(),
                    onSelect = { viewModel.setDistanceUnit(it) }
                )
                Spacer(Modifier.height(12.dp))
                SliderSetting(
                    label = "Decimal Places (0-8)",
                    value = settings.decimalPlaces.toFloat(),
                    range = 0f..8f,
                    onValueChange = { viewModel.setDecimals(it.toInt()) }
                )
            }
        }

        item {
            Divider(modifier = Modifier.padding(vertical = 4.dp))
        }

        item {
            SettingsCard(
                title = "GPS",
                subtitle = "Location tracking precision",
                icon = Icons.Filled.GpsFixed
            ) {
                SliderSetting(
                    label = "Accuracy Threshold (m)",
                    value = settings.gpsAccuracyThreshold,
                    range = 3f..50f,
                    onValueChange = { viewModel.setAccuracy(it) }
                )
                Spacer(Modifier.height(12.dp))
                SliderSetting(
                    label = "Min Distance (m)",
                    value = settings.gpsMinDistance,
                    range = 0.5f..20f,
                    onValueChange = { viewModel.setMinDist(it) }
                )
                Spacer(Modifier.height(12.dp))
                SliderSetting(
                    label = "Update Interval (s)",
                    value = settings.gpsIntervalSec.toFloat(),
                    range = 1f..30f,
                    onValueChange = { viewModel.setInterval(it.toLong()) }
                )
            }
        }

        item {
            Divider(modifier = Modifier.padding(vertical = 4.dp))
        }

        item {
            SettingsCard(
                title = "Map",
                subtitle = "Tile caching and rendering",
                icon = Icons.Filled.Map
            ) {
                SliderSetting(
                    label = "Cache Size (MB)",
                    value = settings.cacheSizeMb.toFloat(),
                    range = 50f..2048f,
                    onValueChange = { viewModel.setCacheSize(it.toInt()) }
                )
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { viewModel.clearCache() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isClearingCache
                ) {
                    Icon(Icons.Filled.Storage, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (uiState.isClearingCache) "Clearing..." else "Clear Map Cache")
                }
            }
        }

        item {
            Divider(modifier = Modifier.padding(vertical = 4.dp))
        }

        item {
            SettingsCard(
                title = "Appearance",
                subtitle = "Visual customization",
                icon = Icons.Filled.Palette
            ) {
                SwitchSetting(
                    label = "Dark Mode",
                    checked = settings.darkMode,
                    onCheckedChange = { viewModel.setDarkMode(it) },
                    icon = Icons.Filled.DarkMode
                )
                SwitchSetting(
                    label = "Dynamic Colors",
                    checked = settings.dynamicColor,
                    onCheckedChange = { viewModel.setDynamicColor(it) },
                    icon = Icons.Filled.Palette
                )
                SwitchSetting(
                    label = "Show GPS Panel",
                    checked = settings.showGpsPanel,
                    onCheckedChange = { viewModel.setGpsPanel(it) },
                    icon = Icons.Filled.Visibility
                )
            }
        }

        item {
            Divider(modifier = Modifier.padding(vertical = 4.dp))
        }

        item {
            SettingsCard(
                title = "Data & Privacy",
                subtitle = "Backup, restore, and manage your data",
                icon = Icons.Filled.DataObject
            ) {
                Button(
                    onClick = { exportLauncher.launch("GeoMeasure_backup.gmbackup") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isBackingUp
                ) {
                    Icon(Icons.Filled.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (uiState.isBackingUp) "Exporting..." else "Export Encrypted Backup")
                }
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { importLauncher.launch(arrayOf("application/octet-stream", "*/*")) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isRestoring
                ) {
                    Icon(Icons.Filled.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (uiState.isRestoring) "Restoring..." else "Restore from Backup")
                }
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Icon(Icons.Filled.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Delete All Data")
                }
            }
        }

        item {
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun SettingsCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun SwitchSetting(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: ImageVector? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val bgColor by animateColorAsState(
        targetValue = if (isPressed)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        else
            MaterialTheme.colorScheme.surface,
        animationSpec = tween(150),
        label = "bg"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onCheckedChange(!checked) }
            .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(12.dp))
            }
            Text(
                label,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.padding(start = 8.dp),
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@Composable
fun SliderSetting(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                if (value == value.toInt().toFloat()) value.toInt().toString()
                else "%.1f".format(value),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(4.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                activeTickColor = MaterialTheme.colorScheme.primary,
                inactiveTickColor = MaterialTheme.colorScheme.outlineVariant
            )
        )
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

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(6.dp))
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
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
