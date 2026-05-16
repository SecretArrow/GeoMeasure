package com.geomeasure.pro.data.export

import android.content.Context
import android.graphics.Bitmap
import com.geomeasure.pro.core.geometry.CoordinateFormatter
import com.geomeasure.pro.core.geometry.UtmProjection
import com.geomeasure.pro.core.util.FileUtils
import com.geomeasure.pro.core.util.formatDecimals
import com.geomeasure.pro.data.local.db.entities.ProjectEntity
import com.geomeasure.pro.data.local.db.entities.VertexEntity
import com.itextpdf.text.*
import com.itextpdf.text.pdf.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.Date

class PdfReportGenerator(private val context: Context) {

    fun generate(
        project: ProjectEntity,
        vertices: List<VertexEntity>,
        mapBitmap: Bitmap?
    ): File {
        val file = FileUtils.createTempFile(context, project.name, "_laporan.pdf")
        val document = Document(PageSize.A4, 36f, 36f, 72f, 72f)
        val fos = FileOutputStream(file)
        PdfWriter.getInstance(document, fos)
        document.open()

        try {
            // === TITLE ===
            val titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22f, BaseColor(46, 125, 50))
            document.add(Paragraph("GeoMeasure Pro", titleFont))
            val subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 12f, BaseColor.GRAY)
            document.add(Paragraph("Laporan Pengukuran Tanah / Land Measurement Report", subtitleFont))
            document.add(Chunk.NEWLINE)

            // === PROJECT INFO ===
            val headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14f, BaseColor.BLACK)
            document.add(Paragraph(project.name, headerFont))
            val infoFont = FontFactory.getFont(FontFactory.HELVETICA, 10f)
            document.add(Paragraph("Tanggal: ${Date(project.createdAt)}", infoFont))
            document.add(Paragraph("Catatan: ${project.notes.ifEmpty { "—" }}", infoFont))
            document.add(Chunk.NEWLINE)

            // === MAP SCREENSHOT ===
            mapBitmap?.let {
                val stream = ByteArrayOutputStream()
                it.compress(Bitmap.CompressFormat.PNG, 90, stream)
                val image = Image.getInstance(stream.toByteArray())
                image.scaleToFit(520f, 320f)
                image.alignment = Image.ALIGN_CENTER
                document.add(image)
                document.add(Chunk.NEWLINE)
            }

            // === SUMMARY TABLE ===
            val summaryTable = PdfPTable(2).apply { widthPercentage = 100f }
            fun addRow(label: String, value: String) {
                val cell = PdfPCell(Phrase(label, FontFactory.getFont(FontFactory.HELVETICA, 10f, BaseColor.BLACK)))
                cell.backgroundColor = BaseColor(232, 237, 232)
                cell.setPadding(6f)
                summaryTable.addCell(cell)
                summaryTable.addCell(PdfPCell(Phrase(value, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10f))))
            }
            addRow("Luas / Area", "${project.areaM2.formatDecimals(4)} m\u00B2  (${(project.areaM2 * 1e-4).formatDecimals(4)} ha)")
            addRow("Keliling / Perimeter", "${project.perimeterM.formatDecimals(2)} m")
            addRow("Jumlah Titik / Vertices", "${vertices.size}")
            addRow("Tipe / Type", if (project.measurementType == "POLYGON") "Poligon / Polygon" else "Garis / Line")
            document.add(summaryTable)
            document.add(Chunk.NEWLINE)

            // === COORDINATE TABLE ===
            document.add(Paragraph("Koordinat / Coordinates", headerFont))
            document.add(Chunk.NEWLINE)

            val coordTable = PdfPTable(5).apply { widthPercentage = 100f }
            val headerCell = PdfPCell(Phrase("", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9f)))
            headerCell.backgroundColor = BaseColor(46, 125, 50)
            headerCell.setPadding(5f)

            listOf("Titik", "Latitude (DMS)", "Longitude (DMS)", "UTM Easting", "UTM Northing").forEach { col ->
                val cell = PdfPCell(Phrase(col, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9f, BaseColor.WHITE)))
                cell.backgroundColor = BaseColor(46, 125, 50)
                cell.setPadding(5f)
                coordTable.addCell(cell)
            }

            val sortedVertices = vertices.sortedBy { it.order }
            sortedVertices.forEachIndexed { i, v ->
                val latDms = CoordinateFormatter.formatDms(v.latitude, true)
                val lonDms = CoordinateFormatter.formatDms(v.longitude, false)
                val (east, north, zone) = UtmProjection.toUtm(v.latitude, v.longitude)
                val hemi = if (v.latitude < 0) "S" else "N"

                coordTable.addCell(PdfPCell(Phrase("P${i + 1}", FontFactory.getFont(FontFactory.HELVETICA, 9f))))
                coordTable.addCell(PdfPCell(Phrase(latDms, FontFactory.getFont(FontFactory.HELVETICA, 8f))))
                coordTable.addCell(PdfPCell(Phrase(lonDms, FontFactory.getFont(FontFactory.HELVETICA, 8f))))
                coordTable.addCell(PdfPCell(Phrase("${"%.2f".format(east)}", FontFactory.getFont(FontFactory.HELVETICA, 8f))))
                coordTable.addCell(PdfPCell(Phrase("${"%.2f".format(north)} (${zone}${hemi})", FontFactory.getFont(FontFactory.HELVETICA, 8f))))
            }
            document.add(coordTable)
            document.add(Chunk.NEWLINE)

            // === BEARING & DISTANCE TABLE ===
            if (sortedVertices.size >= 2) {
                document.add(Paragraph("Arah & Jarak / Bearings & Distances", headerFont))
                document.add(Chunk.NEWLINE)

                val bearingTable = PdfPTable(5).apply { widthPercentage = 100f }
                listOf("Dari", "Ke", "Azimuth", "Arah", "Jarak").forEach { col ->
                    val cell = PdfPCell(Phrase(col, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9f, BaseColor.WHITE)))
                    cell.backgroundColor = BaseColor(21, 101, 192)
                    cell.setPadding(5f)
                    bearingTable.addCell(cell)
                }

                for (i in 0 until sortedVertices.size) {
                    val from = sortedVertices[i]
                    val to = sortedVertices[(i + 1) % sortedVertices.size]
                    val bearing = CoordinateFormatter.bearingBetween(from.latitude, from.longitude, to.latitude, to.longitude)

                    bearingTable.addCell(PdfPCell(Phrase("P${i + 1}", FontFactory.getFont(FontFactory.HELVETICA, 9f))))
                    bearingTable.addCell(PdfPCell(Phrase("P${(i % sortedVertices.size) + 1}", FontFactory.getFont(FontFactory.HELVETICA, 9f))))
                    bearingTable.addCell(PdfPCell(Phrase(bearing.azimuthDms, FontFactory.getFont(FontFactory.HELVETICA, 9f))))
                    bearingTable.addCell(PdfPCell(Phrase(bearing.bearingLabel, FontFactory.getFont(FontFactory.HELVETICA, 9f))))
                    bearingTable.addCell(PdfPCell(Phrase("${bearing.distanceM.formatDecimals(2)} m", FontFactory.getFont(FontFactory.HELVETICA, 9f))))
                }
                document.add(bearingTable)
                document.add(Chunk.NEWLINE)
            }

            // === NOTES AREA ===
            document.add(Paragraph("Catatan Ukur / Survey Notes", headerFont))
            document.add(Chunk.NEWLINE)

            val notesTable = PdfPTable(1).apply { widthPercentage = 100f }
            val notesCell = PdfPCell()
            notesCell.fixedHeight = 200f
            notesCell.borderColor = BaseColor.GRAY
            notesCell.borderWidth = 1f
            notesTable.addCell(notesCell)
            document.add(notesTable)
            document.add(Chunk.NEWLINE)

        } finally {
            try { document.close() } catch (_: Exception) {}
            try { fos.close() } catch (_: Exception) {}
        }

        return file
    }
}
