package com.geomeasure.pro.domain.usecase

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.geomeasure.pro.data.export.CsvExporter
import com.geomeasure.pro.data.export.GeoJsonExporter
import com.geomeasure.pro.data.export.GpxExporter
import com.geomeasure.pro.data.export.KmlExporter
import com.geomeasure.pro.data.export.PdfReportGenerator
import com.geomeasure.pro.data.local.db.entities.ProjectEntity
import com.geomeasure.pro.data.local.db.entities.VertexEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

class ExportProjectUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun exportGeoJson(project: ProjectEntity, vertices: List<VertexEntity>): String =
        GeoJsonExporter().export(project, vertices)

    fun exportKml(project: ProjectEntity, vertices: List<VertexEntity>): String =
        KmlExporter().export(project, vertices)

    fun exportGpx(project: ProjectEntity, vertices: List<VertexEntity>): String =
        GpxExporter().export(project, vertices)

    fun exportCsv(project: ProjectEntity, vertices: List<VertexEntity>): String =
        CsvExporter().export(project, vertices)

    fun exportPdf(project: ProjectEntity, vertices: List<VertexEntity>, mapBitmap: Bitmap?): File =
        PdfReportGenerator(context).generate(project, vertices, mapBitmap)
}
