package com.geomeasure.pro.presentation.screens.tools

import android.widget.Toast
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.geomeasure.pro.core.util.UnitConverter
import com.geomeasure.pro.core.util.formatDecimals

@Composable
fun ToolsScreen(
    viewModel: ExportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.importFile(it)
            Toast.makeText(context, "Importing file...", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            kotlinx.coroutines.delay(5000)
            viewModel.clearMessages()
        }
    }
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessages()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Tools", style = MaterialTheme.typography.headlineMedium)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Export / Import", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Export your measurements as GeoJSON, KML, GPX, CSV, or PDF. " +
                                "Import KML, GeoJSON, and GPX files.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (uiState.projects.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text("Select project:", style = MaterialTheme.typography.labelLarge)

                        var expanded by remember { mutableStateOf(false) }
                        OutlinedButton(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(uiState.selectedProject?.name ?: "Choose project")
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            uiState.projects.forEach { project ->
                                DropdownMenuItem(
                                    text = { Text(project.name) },
                                    onClick = {
                                        viewModel.selectProject(project)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    if (uiState.selectedProject != null) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { viewModel.exportGeoJson() },
                                modifier = Modifier.weight(1f),
                                enabled = !uiState.isExporting
                            ) {
                                Text("GeoJSON")
                            }
                            Spacer(Modifier.width(8.dp))
                            Button(
                                onClick = { viewModel.exportKml() },
                                modifier = Modifier.weight(1f),
                                enabled = !uiState.isExporting
                            ) {
                                Text("KML")
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { viewModel.exportGpx() },
                                modifier = Modifier.weight(1f),
                                enabled = !uiState.isExporting
                            ) {
                                Text("GPX")
                            }
                            Spacer(Modifier.width(8.dp))
                            Button(
                                onClick = { viewModel.exportCsv() },
                                modifier = Modifier.weight(1f),
                                enabled = !uiState.isExporting
                            ) {
                                Text("CSV")
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.exportPdf() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isExporting
                        ) {
                            Text("Export PDF Report")
                        }
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {
                                viewModel.exportSHP(uiState.selectedProject?.id ?: return@Button)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isExporting && uiState.selectedProject != null
                        ) {
                            Text("Export SHP (Shapefile)")
                        }

                    if (uiState.exportedFile != null) {
                        Spacer(Modifier.height(8.dp))
                        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = when (uiState.exportFormat) {
                                "PDF" -> "application/pdf"
                                else -> "text/plain"
                            }
                            putExtra(android.content.Intent.EXTRA_STREAM, uiState.exportedFile)
                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        Button(
                            onClick = {
                                try {
                                    context.startActivity(android.content.Intent.createChooser(shareIntent, "Share"))
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Share failed: ${e.message ?: "Unknown error"}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Share ${uiState.exportFormat ?: "File"}")
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { importLauncher.launch("*/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Import File")
                        }
                    }
                }
            }
        }

        item {
            UnitConverterSection()
        }
    }
}

@Composable
fun UnitConverterSection() {
    var inputValue by remember { mutableStateOf("") }
    var fromUnit by remember { mutableStateOf<Any>(UnitConverter.AreaUnit.SQUARE_METRE) }
    var toUnit by remember { mutableStateOf<Any>(UnitConverter.AreaUnit.HECTARE) }
    var convertedValue by remember { mutableStateOf("") }
    var useArea by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Unit Converter", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            Row {
                OutlinedButton(
                    onClick = { useArea = true },
                    modifier = Modifier.weight(1f),
                    colors = if (useArea) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors()
                ) {
                    Text("Area")
                }
                Spacer(Modifier.width(8.dp))
                OutlinedButton(
                    onClick = { useArea = false },
                    modifier = Modifier.weight(1f),
                    colors = if (!useArea) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors()
                ) {
                    Text("Distance")
                }
            }

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = inputValue,
                onValueChange = { inputValue = it.filter { c -> c.isDigit() || c == '.' }.let { s -> if (s.count { c -> c == '.' } > 1) s.dropLastWhile { c -> c != '.' } + "" else s }; convert(it, fromUnit, toUnit, useArea) { convertedValue = it } },
                label = { Text("Value") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("From:", style = MaterialTheme.typography.labelMedium)
                    UnitSelector(
                        current = fromUnit,
                        useArea = useArea,
                        onSelect = { fromUnit = it; convert(inputValue, it, toUnit, useArea) { convertedValue = it } }
                    )
                }
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("To:", style = MaterialTheme.typography.labelMedium)
                    UnitSelector(
                        current = toUnit,
                        useArea = useArea,
                        onSelect = { toUnit = it; convert(inputValue, fromUnit, it, useArea) { convertedValue = it } }
                    )
                }
            }

            if (convertedValue.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text("Result:", style = MaterialTheme.typography.labelLarge)
                Text(
                    convertedValue,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private fun convert(
    input: String,
    from: Any,
    to: Any,
    useArea: Boolean,
    onResult: (String) -> Unit
) {
    val value = input.toDoubleOrNull() ?: return
    val result = if (useArea) {
        val fromUnit = from as? UnitConverter.AreaUnit ?: return
        val toUnit = to as? UnitConverter.AreaUnit ?: return
        val inSqm = value / fromUnit.factor
        UnitConverter.convertArea(inSqm, toUnit)
    } else {
        val fromUnit = from as? UnitConverter.DistanceUnit ?: return
        val toUnit = to as? UnitConverter.DistanceUnit ?: return
        val inM = value / fromUnit.factor
        UnitConverter.convertDistance(inM, toUnit)
    }
    val toSymbol = if (useArea) {
        (to as? UnitConverter.AreaUnit)?.symbol ?: return
    } else {
        (to as? UnitConverter.DistanceUnit)?.symbol ?: return
    }
    onResult("${result.formatDecimals(4)} $toSymbol")
}

@Composable
fun UnitSelector(
    current: Any,
    useArea: Boolean,
    onSelect: (Any) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val items = if (useArea) UnitConverter.AreaUnit.values().toList() else UnitConverter.DistanceUnit.values().toList()

    OutlinedButton(
        onClick = { expanded = true },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            (current as? UnitConverter.AreaUnit)?.symbol
                ?: (current as? UnitConverter.DistanceUnit)?.symbol
                ?: ""
        )
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
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
