package com.geomeasure.pro.core.map

import android.content.Context
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.MapTileIndex
import java.io.File
import kotlin.math.*

class TileDownloadManager(private val context: Context) {

    fun downloadRegion(
        boundingBox: BoundingBox,
        minZoom: Int,
        maxZoom: Int,
        tileSourceName: String = "MAPNIK",
        onProgress: (Int, Int) -> Unit,
        onComplete: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        try {
            val tileSource = when (tileSourceName) {
                "MAPNIK" -> TileSourceFactory.MAPNIK
                "USGS_TOPO" -> TileSourceFactory.USGS_TOPO
                "OPEN_TOPO" -> TileSourceFactory.OpenTopo
                else -> TileSourceFactory.MAPNIK
            }

            val tileList = generateTileList(boundingBox, minZoom, maxZoom)
            val total = tileList.size
            var completed = 0
            val config = Configuration.getInstance()
            val cacheDir = config.osmdroidTileCache ?: File(context.cacheDir, "osm_tiles")
            cacheDir.mkdirs()

            for (tile in tileList) {
                val zoom = MapTileIndex.getZoom(tile)
                val x = MapTileIndex.getX(tile)
                val y = MapTileIndex.getY(tile)

                val tileFile = File(cacheDir, "$zoom/$x/$y.png")
                if (tileFile.exists()) {
                    completed++
                    onProgress(completed, total)
                    continue
                }

                try {
                    val url = tileSource.getTileURLString(tile)
                    val connection = java.net.URL(url).openConnection()
                    connection.setRequestProperty("User-Agent", Configuration.getInstance().userAgentValue)
                    connection.connectTimeout = 10000
                    connection.readTimeout = 10000
                    connection.getInputStream().use { inputStream ->
                        tileFile.parentFile?.mkdirs()
                        tileFile.outputStream().use { output ->
                            inputStream.copyTo(output)
                        }
                    }
                } catch (e: Exception) {
                    // Skip failed tiles
                }

                completed++
                onProgress(completed, total)
            }

            onComplete()
        } catch (e: Exception) {
            onError(e)
        }
    }

    private fun generateTileList(
        bb: BoundingBox,
        minZoom: Int,
        maxZoom: Int
    ): List<Long> {
        val tiles = mutableListOf<Long>()
        for (zoom in minZoom..maxZoom) {
            val xMin = lon2tile(bb.lonWest, zoom)
            val xMax = lon2tile(bb.lonEast, zoom)
            val yMin = lat2tile(bb.latNorth, zoom)
            val yMax = lat2tile(bb.latSouth, zoom)
            for (x in xMin..xMax) for (y in yMin..yMax)
                tiles.add(MapTileIndex.getTileIndex(zoom, x, y))
        }
        return tiles
    }

    private fun lon2tile(lon: Double, zoom: Int): Int =
        floor((lon + 180.0) / 360.0 * (1 shl zoom)).toInt()

    private fun lat2tile(lat: Double, zoom: Int): Int {
        val latRad = Math.toRadians(lat)
        return floor((1.0 - ln(tan(latRad) + 1.0 / cos(latRad)) / PI) / 2.0 * (1 shl zoom)).toInt()
    }

    private fun tile2lon(x: Int, zoom: Int): Double =
        x.toDouble() / (1 shl zoom) * 360.0 - 180.0

    private fun tile2lat(y: Int, zoom: Int): Double {
        val n = PI - 2.0 * PI * y / (1 shl zoom)
        return Math.toDegrees(atan(0.5 * (exp(n) - exp(-n))))
    }
}
