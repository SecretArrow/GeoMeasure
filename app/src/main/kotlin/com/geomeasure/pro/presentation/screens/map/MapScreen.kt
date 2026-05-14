package com.geomeasure.pro.presentation.screens.map

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.geomeasure.pro.data.local.db.entities.VertexEntity
import com.geomeasure.pro.presentation.components.GpsStatusPanel
import com.geomeasure.pro.presentation.components.MeasurementBottomSheet
import com.geomeasure.pro.core.util.formatDecimals
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel = hiltViewModel(),
    projectId: String? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scaffoldState: BottomSheetScaffoldState = rememberBottomSheetScaffoldState()
    val lifecycleOwner = LocalLifecycleOwner.current
    var permissionLaunched by remember { mutableStateOf(false) }
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var isFollowing by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(context, "Location permission required for My Location", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(projectId) {
        if (projectId != null) {
            viewModel.loadProject(projectId)
        } else if (uiState.currentProject == null) {
            viewModel.createNewProject()
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 100.dp,
        sheetContent = {
            MeasurementBottomSheet(
                areaM2 = uiState.areaM2,
                perimeterM = uiState.perimeterM,
                vertices = uiState.vertices,
                project = uiState.currentProject,
                expanded = true,
                onShareScreenshot = {
                    val mv = mapViewRef ?: return@MeasurementBottomSheet
                    val project = uiState.currentProject ?: return@MeasurementBottomSheet
                    try {
                        val bmp = Bitmap.createBitmap(mv.width, mv.height, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(bmp)
                        mv.draw(canvas)

                        val paint = Paint().apply {
                            color = AndroidColor.BLACK
                            textSize = 40f
                            isAntiAlias = true
                            typeface = Typeface.DEFAULT_BOLD
                        }
                        val whiteBg = Paint().apply {
                            color = AndroidColor.argb(180, 255, 255, 255)
                            style = Paint.Style.FILL
                        }
                        val lineHeight = 50f
                        val margin = 20f
                        val lines = mutableListOf<String>()
                        lines.add(project.name)
                        lines.add("Area: ${uiState.areaM2.formatDecimals(2)} m2")
                        lines.add("Perimeter: ${uiState.perimeterM.formatDecimals(2)} m")
                        lines.add("Vertices: ${uiState.vertices.size}")
                        uiState.vertices.sortedBy { it.order }.forEachIndexed { i, v ->
                            lines.add("${i+1}. ${v.latitude.formatDecimals(6)}, ${v.longitude.formatDecimals(6)}")
                        }

                        val textHeight = lines.size * lineHeight + 40
                        canvas.drawRect(0f, 0f, bmp.width.toFloat(), textHeight, whiteBg)
                        var y = 50f
                        lines.forEach { line ->
                            if (line == lines.first()) {
                                paint.textSize = 48f
                                paint.typeface = Typeface.DEFAULT_BOLD
                            } else {
                                paint.textSize = 36f
                                paint.typeface = Typeface.DEFAULT
                            }
                            canvas.drawText(line, margin, y, paint)
                            y += lineHeight
                        }

                        val file = java.io.File(context.cacheDir, "screenshots")
                        file.mkdirs()
                        val imageFile = java.io.File(file, "${project.name}.png")
                        imageFile.outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
                        bmp.recycle()

                        val uri = androidx.core.content.FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            imageFile
                        )
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "image/png"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            putExtra(Intent.EXTRA_TEXT, "${project.name} - Area: ${uiState.areaM2.formatDecimals(2)} m2")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share Screenshot"))
                    } catch (e: Exception) {
                        Toast.makeText(context, "Screenshot failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    ) { padding ->
        Scaffold(
            modifier = Modifier.padding(padding),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            uiState.currentProject?.name ?: "GeoMeasure Pro",
                            maxLines = 1
                        )
                    },
                    actions = {
                        IconButton(onClick = { viewModel.undo() }) {
                            Icon(Icons.AutoMirrored.Filled.Undo, "Undo")
                        }
                        IconButton(onClick = { viewModel.redo() }) {
                            Icon(Icons.AutoMirrored.Filled.Redo, "Redo")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                var previousVertices by remember { mutableStateOf<List<VertexEntity>?>(null) }
                var myLocationOverlay by remember { mutableStateOf<MyLocationNewOverlay?>(null) }

                AndroidView(
                    factory = { ctx ->
                        MapView(ctx).also { mv ->
                            mapViewRef = mv
                            mv.setTileSource(TileSourceFactory.MAPNIK)
                            mv.setMultiTouchControls(true)
                            mv.controller.setZoom(18.0)

                            mv.overlays.add(
                                MapEventsOverlay(object : MapEventsReceiver {
                                    override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                                        if (viewModel.uiState.value.measurementType == MeasurementType.TAP) {
                                            viewModel.addVertex(p.latitude, p.longitude)
                                        }
                                        return true
                                    }

                                    override fun longPressHelper(p: GeoPoint): Boolean = false
                                })
                            )

                            if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION)
                                == PackageManager.PERMISSION_GRANTED
                            ) {
                                val provider = GpsMyLocationProvider(ctx)
                                MyLocationNewOverlay(provider, mv).also { overlay ->
                                    overlay.enableMyLocation()
                                    overlay.enableFollowLocation()
                                    overlay.runOnFirstFix {
                                        mv.controller.animateTo(overlay.myLocation)
                                    }
                                    mv.overlays.add(overlay)
                                    myLocationOverlay = overlay
                                }
                            }
                        }
                    },
                    update = { mv ->
                        mapViewRef = mv
                        val sorted = uiState.vertices.sortedBy { it.order }
                        if (sorted != previousVertices) {
                            previousVertices = sorted
                            while (mv.overlays.size > 1 + if (myLocationOverlay != null) 1 else 0) {
                                mv.overlays.removeAt(mv.overlays.lastIndex)
                            }
                            sorted.forEach { vertex ->
                                Marker(mv).apply {
                                    position = GeoPoint(vertex.latitude, vertex.longitude)
                                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                    isDraggable = true
                                    title = "${vertex.order + 1}"
                                    setOnMarkerDragListener(object : Marker.OnMarkerDragListener {
                                        override fun onMarkerDragStart(marker: Marker) {}
                                        override fun onMarkerDrag(marker: Marker) {}
                                        override fun onMarkerDragEnd(marker: Marker) {
                                            viewModel.updateVertex(
                                                vertex.copy(
                                                    latitude = marker.position.latitude,
                                                    longitude = marker.position.longitude
                                                )
                                            )
                                        }
                                    })
                                    mv.overlays.add(this)
                                }
                            }
                            if (sorted.size >= 2) {
                                Polygon().apply {
                                    points = sorted.map { GeoPoint(it.latitude, it.longitude) }
                                    fillColor = AndroidColor.argb(75, 33, 150, 243)
                                    strokeColor = AndroidColor.argb(255, 33, 150, 243)
                                    strokeWidth = 3f
                                    mv.overlays.add(this)
                                }
                            }
                            mv.invalidate()
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                LaunchedEffect(mapViewRef) {
                    mapViewRef?.onResume()
                }

                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        when (event) {
                            Lifecycle.Event.ON_RESUME -> {
                                mapViewRef?.onResume()
                                myLocationOverlay?.enableMyLocation()
                            }
                            Lifecycle.Event.ON_PAUSE -> {
                                mapViewRef?.onPause()
                                myLocationOverlay?.disableMyLocation()
                            }
                            else -> {}
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                        myLocationOverlay?.disableMyLocation()
                        mapViewRef?.onDetach()
                    }
                }

                if (uiState.gpsStatus.accuracyM > 0f) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        GpsStatusPanel(
                            accuracyM = uiState.gpsStatus.accuracyM,
                            satellitesUsed = uiState.gpsStatus.satellitesUsed,
                            satellitesInView = uiState.gpsStatus.satellitesInView,
                            hdop = uiState.gpsStatus.hdop,
                            altitudeM = uiState.gpsStatus.altitudeM
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MeasurementType.entries.forEach { type ->
                            FloatingActionButton(
                                onClick = { viewModel.setMeasurementType(type) },
                                modifier = Modifier.size(36.dp),
                                containerColor = if (uiState.measurementType == type)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (uiState.measurementType == type)
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            ) {
                                Text(
                                    text = when (type) {
                                        MeasurementType.TAP -> "T"
                                        MeasurementType.WALK -> "W"
                                        MeasurementType.LINE -> "L"
                                        MeasurementType.POINT -> "P"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    FloatingActionButton(
                        onClick = {
                            if (ContextCompat.checkSelfPermission(
                                    context, Manifest.permission.ACCESS_FINE_LOCATION
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                return@FloatingActionButton
                            }
                            myLocationOverlay?.let { overlay ->
                                if (overlay.myLocation != null) {
                                    isFollowing = !isFollowing
                                    if (isFollowing) {
                                        overlay.enableFollowLocation()
                                        mapViewRef?.controller?.animateTo(overlay.myLocation)
                                    } else {
                                        overlay.disableFollowLocation()
                                    }
                                } else {
                                    overlay.runOnFirstFix {
                                        overlay.enableFollowLocation()
                                        mapViewRef?.controller?.animateTo(overlay.myLocation)
                                        isFollowing = true
                                    }
                                }
                            }
                        },
                        containerColor = if (isFollowing)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isFollowing)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    ) {
                        Icon(Icons.Filled.MyLocation, "My Location")
                    }

                    FloatingActionButton(
                        onClick = {
                            if (uiState.isRecording) {
                                viewModel.saveProject()
                                viewModel.setRecording(false)
                                Toast.makeText(context, "Measurement saved", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.createNewProject()
                                viewModel.setRecording(true)
                            }
                        },
                        containerColor = if (uiState.isRecording)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            if (uiState.isRecording) Icons.Filled.Stop else Icons.Filled.Add,
                            contentDescription = if (uiState.isRecording) "Stop" else "Start"
                        )
                    }
                }
            }
        }
    }
}
