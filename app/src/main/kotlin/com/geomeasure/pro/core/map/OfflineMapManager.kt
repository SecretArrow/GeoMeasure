package com.geomeasure.pro.core.map

import android.content.Context
import org.osmdroid.config.Configuration
import java.io.File

class OfflineMapManager(private val context: Context) {

    val tileCacheDir: File
        get() = Configuration.getInstance().osmdroidTileCache ?: File(context.cacheDir, "osm_tiles")

    fun getCacheSize(): Long {
        return tileCacheDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
    }

    fun clearCache(): Boolean {
        return tileCacheDir.deleteRecursively().also {
            tileCacheDir.mkdirs()
        }
    }

    fun importMbtilesFile(source: File): Boolean {
        val dest = File(tileCacheDir, source.name)
        return source.copyTo(dest, overwrite = true).exists()
    }
}
