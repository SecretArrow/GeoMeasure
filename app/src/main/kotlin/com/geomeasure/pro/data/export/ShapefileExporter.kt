package com.geomeasure.pro.data.export

import android.content.Context
import com.geomeasure.pro.core.util.FileUtils
import com.geomeasure.pro.data.local.db.entities.ProjectEntity
import com.geomeasure.pro.data.local.db.entities.VertexEntity
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset

class ShapefileExporter {

    fun export(
        project: ProjectEntity,
        vertices: List<VertexEntity>,
        context: Context
    ): Triple<File, File, File> {
        val baseName = project.name.replace(Regex("[^a-zA-Z0-9_\\- ]"), "_")
        val shpFile = FileUtils.createTempFile(context, "${baseName}_", ".shp")
        val shxFile = File(shpFile.parent, shpFile.nameWithoutExtension + ".shx")
        val dbfFile = File(shpFile.parent, shpFile.nameWithoutExtension + ".dbf")

        val sorted = vertices.sortedBy { it.order }
        val numPoints = sorted.size + 1
        val points = mutableListOf<Pair<Double, Double>>()
        sorted.forEach { points.add(it.longitude to it.latitude) }
        if (points.isNotEmpty()) points.add(points[0])

        val numParts = 1
        val contentLengthWords = 50 + numPoints * 8
        val fileLengthWords = 50 + contentLengthWords

        // Write .shp
        writeShapefile(shpFile, points, contentLengthWords, fileLengthWords, numParts, numPoints)
        // Write .shx
        writeIndexFile(shxFile, contentLengthWords, fileLengthWords, numParts, numPoints)
        // Write .dbf
        writeDbf(dbfFile, project, sorted)

        return Triple(shpFile, shxFile, dbfFile)
    }

    private fun writeShapefile(
        file: File, points: List<Pair<Double, Double>>,
        contentLen: Int, fileLen: Int, numParts: Int, numPoints: Int
    ) {
        val bb = ByteBuffer.allocate(100 + contentLen * 2).apply { order(ByteOrder.BIG_ENDIAN) }

        // Header (100 bytes)
        putShpHeader(bb, fileLen, 5, numParts, numPoints, points)
        bb.order(ByteOrder.LITTLE_ENDIAN)

        // Record header
        bb.putInt(1)            // record number
        bb.putInt(contentLen)   // content length in words

        // Record content: Polygon (type 5)
        bb.putInt(5)            // shape type = Polygon
        // MBR
        val minX = points.minOf { it.first }; val minY = points.minOf { it.second }
        val maxX = points.maxOf { it.first }; val maxY = points.maxOf { it.second }
        bb.putDouble(minX); bb.putDouble(minY); bb.putDouble(maxX); bb.putDouble(maxY)
        bb.putInt(numParts)     // num parts
        bb.putInt(numPoints)    // num points
        bb.putInt(0)            // parts array (index 0)
        points.forEach { (x, y) -> bb.putDouble(x); bb.putDouble(y) }

        RandomAccessFile(file, "rw").use { raf ->
            raf.write(bb.array(), 0, 100 + contentLen * 2)
        }
    }

    private fun writeIndexFile(
        file: File, contentLen: Int, fileLen: Int, numParts: Int, numPoints: Int
    ) {
        val bb = ByteBuffer.allocate(100 + 8).apply { order(ByteOrder.BIG_ENDIAN) }
        putShpHeader(bb, fileLen / 2, 7, numParts, numPoints, emptyList())
        // offset = 50 words (header), content length = contentLen
        bb.order(ByteOrder.BIG_ENDIAN)
        bb.putInt(50); bb.putInt(contentLen)

        RandomAccessFile(file, "rw").use { raf -> raf.write(bb.array()) }
    }

    private fun putShpHeader(
        bb: ByteBuffer, fileLen: Int, shapeType: Int,
        numParts: Int, numPoints: Int, points: List<Pair<Double, Double>>
    ) {
        bb.order(ByteOrder.BIG_ENDIAN)
        bb.putInt(9994); bb.putInt(0); bb.putInt(0); bb.putInt(0); bb.putInt(0); bb.putInt(0)
        bb.putInt(fileLen)
        bb.order(ByteOrder.LITTLE_ENDIAN)
        bb.putInt(1000)     // version
        bb.putInt(shapeType)// 1=point,3=polyline,5=polygon
        val minX = if (points.isEmpty()) 0.0 else points.minOf { it.first }
        val minY = if (points.isEmpty()) 0.0 else points.minOf { it.second }
        val maxX = if (points.isEmpty()) 0.0 else points.maxOf { it.first }
        val maxY = if (points.isEmpty()) 0.0 else points.maxOf { it.second }
        bb.putDouble(minX); bb.putDouble(minY); bb.putDouble(maxX); bb.putDouble(maxY)
        bb.putDouble(0.0); bb.putDouble(0.0); bb.putDouble(0.0); bb.putDouble(0.0)
    }

    private fun writeDbf(file: File, project: ProjectEntity, vertices: List<VertexEntity>) {
        val charset = Charset.forName("ISO-8859-1")
        val records = vertices.sortedBy { it.order }

        // Field definitions
        data class Field(val name: String, val type: Byte, val length: Int, val decimalCount: Int)

        val fields = listOf(
            Field("POINT", 'N'.code.toByte(), 4, 0),
            Field("LAT_DD", 'N'.code.toByte(), 12, 7),
            Field("LON_DD", 'N'.code.toByte(), 12, 7),
            Field("LAT_DMS", 'C'.code.toByte(), 20, 0),
            Field("LON_DMS", 'C'.code.toByte(), 20, 0)
        )

        val headerLen = 33 + fields.size * 32 + 1
        val recordLen = fields.sumOf { it.length } + 1
        val numRecords = records.size

        ByteArrayOutputStream().use { buf ->
            // Header
            buf.write(3)                  // version (dBase III)
            buf.write(0)                  // date YY
            buf.write(0)                  // date MM
            buf.write(0)                  // date DD
            buf.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(numRecords).array())
            buf.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(headerLen.toShort()).array())
            buf.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(recordLen.toShort()).array())
            buf.write(ByteArray(20))      // reserved

            // Field descriptors
            fields.forEach { field ->
                val nameBytes = field.name.toByteArray(charset)
                buf.write(nameBytes.copyOf(11))
                buf.write(field.type.toInt())
                buf.write(ByteArray(4))   // reserved
                buf.write(field.length)
                buf.write(field.decimalCount)
                buf.write(ByteArray(14))  // reserved
            }

            buf.write(0x0D) // header terminator

            // Records
            records.forEachIndexed { i, v ->
                buf.write(0x20) // deleted flag (space = active)
                val latDms = com.geomeasure.pro.core.geometry.CoordinateFormatter.formatDms(v.latitude, true)
                val lonDms = com.geomeasure.pro.core.geometry.CoordinateFormatter.formatDms(v.longitude, false)

                val pt = "${i + 1}".padStart(4)
                val lat = "%.7f".format(v.latitude).take(12)
                val lon = "%.7f".format(v.longitude).take(12)
                val ldms = latDms.take(20)
                val lodms = lonDms.take(20)

                listOf(pt, lat, lon, ldms, lodms).forEach { value ->
                    val bytes = value.toByteArray(charset)
                    buf.write(bytes.copyOf(value.length))
                }
            }

            // EOF marker (0x1A)
            if (buf.size() < headerLen + numRecords * recordLen) {
                buf.write(0x1A)
            }

            RandomAccessFile(file, "rw").use { raf ->
                raf.write(buf.toByteArray())
            }
        }
    }
}
