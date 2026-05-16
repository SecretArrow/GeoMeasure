package com.geomeasure.pro.data.import_data

import android.content.Context
import android.net.Uri
import com.geomeasure.pro.core.util.FileUtils
import com.geomeasure.pro.data.local.db.AppDatabase
import com.geomeasure.pro.data.local.db.entities.ProjectEntity
import com.geomeasure.pro.data.local.db.entities.VertexEntity
import kotlin.math.abs
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

class FileImporter @Inject constructor(private val db: AppDatabase) {

    suspend fun importFile(uri: Uri, context: Context): Result<ProjectEntity> = runCatching {
        val fileName = FileUtils.getFileName(context, uri) ?: "Import"
        val extension = fileName.substringAfterLast('.', "").lowercase()
        val content = context.contentResolver.openInputStream(uri)?.use { stream ->
            stream.bufferedReader().readText()
        } ?: error("Cannot read file")

        val parseResult = when (extension) {
            "geojson", "json" -> parseGeoJson(content, fileName)
            "kml", "kmz" -> parseKml(content, fileName)
            "gpx" -> parseGpx(content, fileName)
            else -> error("Unsupported format: .$extension")
        }
        val project = parseResult.first
        val vertices = parseResult.second
        db.projectDao().insertProject(project)
        vertices.forEach { db.vertexDao().insertVertex(it) }
        project
    }

    private fun parseGeoJson(content: String, fileName: String): Pair<ProjectEntity, List<VertexEntity>> {
        val json = JSONObject(content)

        val features: JSONArray = if (json.has("features")) {
            json.getJSONArray("features")
        } else {
            JSONArray().put(json)
        }

        if (features.length() == 0) error("GeoJSON has no features")
        val feature = features.getJSONObject(0)
        val geometry = feature.optJSONObject("geometry") ?: error("Feature missing geometry")
        val geometryType = geometry.optString("type", "Polygon")
        val props = feature.optJSONObject("properties") ?: JSONObject()

        val projectId = java.util.UUID.randomUUID().toString()
        val vertices = mutableListOf<VertexEntity>()

        val rawCoords = when (geometryType) {
            "Point" -> {
                val c = geometry.getJSONArray("coordinates")
                vertices.add(VertexEntity(
                    projectId = projectId,
                    latitude = coordToDouble(c, 1),
                    longitude = coordToDouble(c, 0),
                    order = 0
                ))
                JSONArray()
            }
            "LineString" -> {
                val arr = geometry.getJSONArray("coordinates")
                if (arr.length() == 0) error("Empty LineString coordinates")
                arr
            }
            "Polygon" -> {
                val arr = geometry.getJSONArray("coordinates")
                if (arr.length() == 0) error("Empty Polygon coordinates")
                arr.getJSONArray(0)
            }
            "MultiPoint" -> geometry.getJSONArray("coordinates")
            "MultiLineString" -> {
                val arr = geometry.getJSONArray("coordinates")
                if (arr.length() == 0) error("Empty MultiLineString coordinates")
                arr.getJSONArray(0)
            }
            "MultiPolygon" -> {
                val arr = geometry.getJSONArray("coordinates")
                if (arr.length() == 0) error("Empty MultiPolygon coordinates")
                arr.getJSONArray(0).getJSONArray(0)
            }
            else -> error("Unsupported geometry type: $geometryType")
        }

        if (rawCoords.length() > 0 && geometryType != "Point") {
            for (i in 0 until rawCoords.length()) {
                val c = rawCoords.getJSONArray(i)
                vertices.add(VertexEntity(
                    projectId = projectId,
                    latitude = coordToDouble(c, 1),
                    longitude = coordToDouble(c, 0),
                    order = i
                ))
            }
        }

        // Remove closing coord if same as first (within tolerance)
        if (vertices.size > 1 &&
            abs(vertices.first().latitude - vertices.last().latitude) < 1e-10 &&
            abs(vertices.first().longitude - vertices.last().longitude) < 1e-10
        ) {
            vertices.removeAt(vertices.lastIndex)
        }

        val project = ProjectEntity(
            id = projectId,
            name = props.optString("name", fileName),
            notes = props.optString("notes", ""),
            areaM2 = props.optDouble("area_m2", 0.0),
            perimeterM = props.optDouble("perimeter_m", 0.0)
        )
        return Pair(project, vertices)
    }

    private fun coordToDouble(coord: JSONArray, index: Int): Double {
        val value = coord.opt(index)
        return when (value) {
            is Number -> value.toDouble()
            is String -> value.toDoubleOrNull() ?: 0.0
            else -> 0.0
        }
    }

    private fun parseKml(content: String, fileName: String): Pair<ProjectEntity, List<VertexEntity>> {
        val projectId = java.util.UUID.randomUUID().toString()
        val name = Regex("<name>([^<]+)</name>").find(content)?.groupValues?.get(1) ?: fileName
        val coordsMatch = Regex("<coordinates>([^<]+)</coordinates>").find(content)
        val coordsStr = coordsMatch?.groupValues?.get(1) ?: ""

        val vertices = coordsStr.trim().takeIf { it.isNotEmpty() }?.split("\\s+".toRegex())
            ?.mapIndexed { i, coord ->
                val parts = coord.split(",")
                if (parts.size < 2) error("Invalid KML coordinate at index $i: '$coord'")
                VertexEntity(
                    projectId = projectId,
                    longitude = parts[0].toDoubleOrNull() ?: error("Invalid longitude at index $i: '${parts[0]}'"),
                    latitude = parts[1].toDoubleOrNull() ?: error("Invalid latitude at index $i: '${parts[1]}'"),
                    altitude = parts.getOrNull(2)?.toDoubleOrNull() ?: 0.0,
                    order = i
                )
            } ?: emptyList()

        val project = ProjectEntity(id = projectId, name = name)
        return Pair(project, vertices)
    }

    private fun parseGpx(content: String, fileName: String): Pair<ProjectEntity, List<VertexEntity>> {
        val projectId = java.util.UUID.randomUUID().toString()
        val name = Regex("<name>([^<]+)</name>").find(content)?.groupValues?.get(1) ?: fileName
        val wptPattern = Regex(
            """<wpt\s+lat="([^"]*)"\s+lon="([^"]*)">\s*<ele>([^<]*)</ele>""".trimMargin()
        )

        val vertices = wptPattern.findAll(content).mapIndexed { i, match ->
            val latStr = match.groupValues[1]
            val lonStr = match.groupValues[2]
            if (latStr.isEmpty() || lonStr.isEmpty()) error("Empty GPX coordinate at index $i")
            VertexEntity(
                projectId = projectId,
                latitude = latStr.toDoubleOrNull() ?: error("Invalid latitude at index $i: '$latStr'"),
                longitude = lonStr.toDoubleOrNull() ?: error("Invalid longitude at index $i: '$lonStr'"),
                altitude = match.groupValues[3].toDoubleOrNull() ?: 0.0,
                order = i
            )
        }.toList()

        val project = ProjectEntity(id = projectId, name = name)
        return Pair(project, vertices)
    }
}
