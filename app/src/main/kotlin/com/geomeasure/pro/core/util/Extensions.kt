package com.geomeasure.pro.core.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Double.formatDecimals(n: Int): String = "%.${n}f".format(this)

fun Long.formatDate(pattern: String = "MMM dd, yyyy HH:mm"): String {
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return sdf.format(Date(this))
}

fun String.escapeJson(): String =
    replace("\"", "\\\"").replace("\n", "\\n")

fun String.escapeXml(): String =
    replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
