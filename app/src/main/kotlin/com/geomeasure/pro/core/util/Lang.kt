package com.geomeasure.pro.core.util

import android.content.Context
import com.geomeasure.pro.R

object Lang {

    enum class Language(val code: String, val displayName: String, val nativeName: String) {
        EN("en", "English", "English"),
        ID("id", "Indonesian", "Bahasa Indonesia")
    }

    fun getString(context: Context, resId: Int, lang: Language = Language.EN): String {
        val config = context.resources.configuration
        val locale = java.util.Locale(lang.code)
        val localizedContext = context.createConfigurationContext(
            android.content.res.Configuration(config).apply { setLocale(locale) }
        )
        return localizedContext.getString(resId)
    }

    fun getString(context: Context, resId: Int, langCode: String): String {
        val lang = Language.values().find { it.code == langCode } ?: Language.EN
        return getString(context, resId, lang)
    }

    fun localizedAreaLabel(context: Context, lang: String): String {
        return if (lang == "id") "Luas" else "Area"
    }

    fun localizedPerimeterLabel(context: Context, lang: String): String {
        return if (lang == "id") "Keliling" else "Perimeter"
    }

    fun localizedVerticesLabel(context: Context, lang: String): String {
        return if (lang == "id") "Titik" else "Vertices"
    }

    fun localizedCoordinatesLabel(context: Context, lang: String): String {
        return if (lang == "id") "Koordinat" else "Coordinates"
    }

    fun localizedProjectLabel(context: Context, lang: String): String {
        return if (lang == "id") "Proyek" else "Project"
    }

    fun localizedNotesLabel(context: Context, lang: String): String {
        return if (lang == "id") "Catatan" else "Notes"
    }

    fun localizedMeasurementLabel(context: Context, lang: String): String {
        return if (lang == "id") "Pengukuran" else "Measurement"
    }

    fun localizedSaveLabel(context: Context, lang: String): String {
        return if (lang == "id") "Simpan" else "Save"
    }

    fun localizedExportLabel(context: Context, lang: String): String {
        return if (lang == "id") "Ekspor" else "Export"
    }

    fun localizedBackupLabel(context: Context, lang: String): String {
        return if (lang == "id") "Cadangan" else "Backup"
    }

    fun localizedRestoreLabel(context: Context, lang: String): String {
        return if (lang == "id") "Pulihkan" else "Restore"
    }
}
