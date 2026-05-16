package com.geomeasure.pro.core.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

object FileUtils {

    fun getFileName(context: Context, uri: Uri): String? {
        var name: String? = null
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) name = it.getString(index)
            }
        }
        return name
    }

    fun getFileSize(context: Context, uri: Uri): Long {
        var size = 0L
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.SIZE)
                if (index >= 0) size = it.getLong(index)
            }
        }
        return size
    }

    fun createTempFile(context: Context, prefix: String, suffix: String): java.io.File {
        val dir = java.io.File(context.cacheDir, "exports")
        dir.mkdirs()
        val safePrefix = prefix.replace(Regex("[/\\\\?%*:|\"<>]"), "_")
        return java.io.File.createTempFile(safePrefix, suffix, dir)
    }
}
