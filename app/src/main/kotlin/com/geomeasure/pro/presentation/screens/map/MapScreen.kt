package com.geomeasure.pro.presentation.screens.map

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
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
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel = hiltViewModel(),
    projectId: String? = null,
    lang: String = "en"
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scaffoldState: BottomSheetScaffoldState = rememberBottomSheetScaffoldState()
    val lifecycleOwner = LocalLifecycleOwner.current
    var permissionLaunched by remember { mutableStateOf(false) }
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var myLocationOverlay by remember { mutableStateOf<MyLocationNewOverlay?>(null) }
    var isFollowing by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            try { myLocationOverlay?.enableMyLocation() } catch (_: SecurityException) {}
        } else {
            Toast.makeText(context, "Location permission required for My Location", Toast.LENGTH_LONG).show()
        }
    }

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.createNewProject()
            viewModel.setRecording(true)
        }
    }

    LaunchedEffect(projectId) {
        if (projectId != null) {
            viewModel.loadProject(projectId)
        } else {
            viewModel.createNewProject()
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 100.dp,
        sheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        sheetContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        sheetShadowElevation = 8.dp,
        sheetDragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                )
            }
        },
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
                    if (mv.width <= 0 || mv.height <= 0) {
                        Toast.makeText(context, "Map not ready yet", Toast.LENGTH_SHORT).show()
                        return@MeasurementBottomSheet
                    }
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
                        val safeName = project.name.replace(Regex("[/\\\\?%*:|\"<>]"), "_")
                        val imageFile = java.io.File(file, "${safeName}.png")
                        imageFile.outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
                        bmp.recycle()

                        val uri = androidx.core.content.FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            imageFile
                        )
                        val shareText = buildString {
                            appendLine(project.name)
                            appendLine("Area: ${uiState.areaM2.formatDecimals(2)} m2  |  Perimeter: ${uiState.perimeterM.formatDecimals(2)} m")
                            appendLine("Vertices: ${uiState.vertices.size} titik")
                            appendLine("")
                            uiState.vertices.sortedBy { it.order }.forEachIndexed { i, v ->
                                appendLine("${i + 1}. ${v.latitude.formatDecimals(6)}, ${v.longitude.formatDecimals(6)}")
                            }
                            appendLine("")
                            appendLine("GeoMeasure Pro — offline, private, free")
                        }
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "image/png"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            putExtra(Intent.EXTRA_TEXT, shareText.toString())
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        try {
                            context.startActivity(Intent.createChooser(intent, "Share Screenshot"))
                        } catch (e: Exception) {
                            Toast.makeText(context, "Share failed: ${e.message ?: "Unknown error"}", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Screenshot failed: ${e.message ?: "Unknown error"}", Toast.LENGTH_SHORT).show()
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
                        SmallFloatingActionButton(
                            onClick = { viewModel.undo() },
                            shape = RoundedCornerShape(12.dp),
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Undo,
                                "Undo",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        SmallFloatingActionButton(
                            onClick = { viewModel.redo() },
                            shape = RoundedCornerShape(12.dp),
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Redo,
                                "Redo",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                    )
                )
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                var previousVertices by remember { mutableStateOf<List<VertexEntity>?>(null) }

                AndroidView(
                    factory = { ctx ->
                        MapView(ctx).also { mv ->
                            mapViewRef = mv
                            mv.setTileSource(TileSourceFactory.MAPNIK)
                            mv.setMultiTouchControls(true)
                            mv.controller.setZoom(18.0)

                            val scaleBar = ScaleBarOverlay(mv).apply {
                                setAlignBottom(true)
                                setAlignRight(true)
                                setScaleBarOffset(20, 40)
                                enableScaleBar()
                                setTextSize(24f)
                            }
                            mv.overlays.add(0, scaleBar)

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

                            val provider = GpsMyLocationProvider(ctx)
                            MyLocationNewOverlay(provider, mv).also { overlay ->
                                if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION)
                                    == PackageManager.PERMISSION_GRANTED
                                ) {
                                    try { overlay.enableMyLocation() } catch (_: SecurityException) {}
                                    try { overlay.enableFollowLocation() } catch (_: SecurityException) {}
                                    overlay.runOnFirstFix {
                                        overlay.myLocation?.let { mv.controller.animateTo(it) }
                                    }
                                }
                                mv.overlays.add(overlay)
                                myLocationOverlay = overlay
                            }
                        }
                    },
                    update = { mv ->
                        mapViewRef = mv
                        val sorted = uiState.vertices.sortedBy { it.order }
                        if (sorted != previousVertices) {
                            previousVertices = sorted
                            val baseCount = 3
                            while (mv.overlays.size > baseCount) {
                                mv.overlays.removeAt(mv.overlays.lastIndex)
                            }
                            sorted.forEachIndexed { index, vertex ->
                                val label = "P${index + 1}"
                                Marker(mv).apply {
                                    position = GeoPoint(vertex.latitude, vertex.longitude)
                                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                    isDraggable = true
                                    title = "$label: ${vertex.latitude.formatDecimals(6)}, ${vertex.longitude.formatDecimals(6)}"
                                    icon = BitmapDrawable(context.resources, createTextIcon(label))
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

                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        when (event) {
                            Lifecycle.Event.ON_RESUME -> {
                                mapViewRef?.onResume()
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                                    == PackageManager.PERMISSION_GRANTED
                                ) {
                                    try { myLocationOverlay?.enableMyLocation() } catch (_: SecurityException) {}
                                }
                            }
                            Lifecycle.Event.ON_PAUSE -> {
                                mapViewRef?.onPause()
                                try { myLocationOverlay?.disableMyLocation() } catch (_: SecurityException) {}
                            }
                            else -> {}
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                        try { myLocationOverlay?.disableMyLocation() } catch (_: SecurityException) {}
                        mapViewRef?.onDetach()
                    }
                }

                if (uiState.gpsStatus.accuracyM > 0f) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                        tonalElevation = 2.dp,
                        shadowElevation = 4.dp
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MeasurementType.entries.forEach { type ->
                            val toolBg by animateColorAsState(
                                targetValue = if (uiState.measurementType == type)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.surfaceVariant,
                                animationSpec = tween(300),
                                label = "toolBg_${type.name}"
                            )

                            SmallFloatingActionButton(
                                onClick = { viewModel.setMeasurementType(type) },
                                shape = RoundedCornerShape(12.dp),
                                containerColor = toolBg,
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
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    SmallFloatingActionButton(
                        onClick = {
                            if (ContextCompat.checkSelfPermission(
                                    context, Manifest.permission.ACCESS_FINE_LOCATION
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                return@SmallFloatingActionButton
                            }
                            val overlay = myLocationOverlay
                            if (overlay != null) {
                                if (!overlay.isMyLocationEnabled && ContextCompat.checkSelfPermission(
                                        context, Manifest.permission.ACCESS_FINE_LOCATION
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    try { overlay.enableMyLocation() } catch (_: SecurityException) {}
                                }
                                if (overlay.myLocation != null) {
                                    isFollowing = !isFollowing
                                    if (isFollowing) {
                                        overlay.enableFollowLocation()
                                        overlay.myLocation?.let { mapViewRef?.controller?.animateTo(it) }
                                    } else {
                                        overlay.disableFollowLocation()
                                    }
                                } else {
                                    overlay.runOnFirstFix {
                                        overlay.enableFollowLocation()
                                        overlay.myLocation?.let { mapViewRef?.controller?.animateTo(it) }
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
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                                    != PackageManager.PERMISSION_GRANTED
                                ) {
                                    notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    return@FloatingActionButton
                                }
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

private fun createTextIcon(text: String): Bitmap {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.WHITE
        textSize = 36f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.parseColor("#1565C0")
        style = Paint.Style.FILL
    }
    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    val textWidth = paint.measureText(text)
    val textHeight = paint.descent() - paint.ascent()
    val padding = 20f
    val w = (textWidth + padding * 2).toInt()
    val h = (textHeight + padding * 2).toInt()
    val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    val r = 8f
    canvas.drawRoundRect(0f, 0f, w.toFloat(), h.toFloat(), r, r, bgPaint)
    canvas.drawRoundRect(0f, 0f, w.toFloat(), h.toFloat(), r, r, strokePaint)
    canvas.drawText(text, w / 2f, h / 2f - (paint.ascent() + paint.descent()) / 2f, paint)
    return bmp
}
