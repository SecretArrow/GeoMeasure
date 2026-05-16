package com.geomeasure.pro.core.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Double.formatDecimals(n: Int): String {
    require(n >= 0) { "Decimal places must be non-negative, got $n" }
    if (isNaN()) return "NaN"
    if (isInfinite()) return if (this > 0) "Infinity" else "-Infinity"
    return "%.${n}f".format(this)
}

fun Long.formatDate(pattern: String = "MMM dd, yyyy HH:mm"): String {
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return sdf.format(Date(this))
}

fun String.escapeJson(): String =
    replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t")

fun String.escapeXml(): String =
    replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")
