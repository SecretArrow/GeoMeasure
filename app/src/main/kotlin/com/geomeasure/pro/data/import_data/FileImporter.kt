package com.geomeasure.pro.data.import_data

import android.content.Context
import android.net.Uri
import androidx.room.Transaction
import com.geomeasure.pro.core.util.FileUtils
import com.geomeasure.pro.data.local.db.AppDatabase
import com.geomeasure.pro.data.local.db.entities.ProjectEntity
import com.geomeasure.pro.data.local.db.entities.VertexEntity
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

class FileImporter @Inject constructor(private val db: AppDatabase) {

    @Transaction
    suspend fun importFile(uri: Uri, context: Context): Result<ProjectEntity> = runCatching<ProjectEntity> {
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
        val features = json.getJSONArray("features")
        val feature = features.getJSONObject(0)
        val geometry = feature.getJSONObject("geometry")
        val coords = geometry.getJSONArray("coordinates").getJSONArray(0)
        val props = feature.optJSONObject("properties") ?: JSONObject()

        val projectId = java.util.UUID.randomUUID().toString()
        val vertices = mutableListOf<VertexEntity>()
        for (i in 0 until coords.length()) {
            val coord = coords.getJSONArray(i)
            vertices.add(
                VertexEntity(
                    projectId = projectId,
                    latitude = coord.getDouble(1),
                    longitude = coord.getDouble(0),
                    order = i
                )
            )
        }
        // Remove closing coord if same as first
        if (vertices.size > 1 && vertices.first().latitude == vertices.last().latitude &&
            vertices.first().longitude == vertices.last().longitude
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

    private fun parseKml(content: String, fileName: String): Pair<ProjectEntity, List<VertexEntity>> {
        val projectId = java.util.UUID.randomUUID().toString()
        val name = Regex("<name>([^<]+)</name>").find(content)?.groupValues?.get(1) ?: fileName
        val coordsMatch = Regex("<coordinates>([^<]+)</coordinates>").find(content)
        val coordsStr = coordsMatch?.groupValues?.get(1) ?: ""

        val vertices = coordsStr.trim().split("\\s+".toRegex()).mapIndexed { i, coord ->
            val parts = coord.split(",")
            VertexEntity(
                projectId = projectId,
                longitude = parts[0].toDouble(),
                latitude = parts[1].toDouble(),
                altitude = parts.getOrNull(2)?.toDouble() ?: 0.0,
                order = i
            )
        }

        val project = ProjectEntity(id = projectId, name = name)
        return Pair(project, vertices)
    }

    private fun parseGpx(content: String, fileName: String): Pair<ProjectEntity, List<VertexEntity>> {
        val projectId = java.util.UUID.randomUUID().toString()
        val name = Regex("<name>([^<]+)</name>").find(content)?.groupValues?.get(1) ?: fileName
        val wptPattern = Regex(
            """<wpt\s+lat="([^"]+)"\s+lon="([^"]+)">\s*<ele>([^<]*)</ele>""".trimMargin()
        )

        val vertices = wptPattern.findAll(content).mapIndexed { i, match ->
            VertexEntity(
                projectId = projectId,
                latitude = match.groupValues[1].toDouble(),
                longitude = match.groupValues[2].toDouble(),
                altitude = match.groupValues[3].toDoubleOrNull() ?: 0.0,
                order = i
            )
        }.toList()

        val project = ProjectEntity(id = projectId, name = name)
        return Pair(project, vertices)
    }
}
