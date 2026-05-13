package com.geomeasure.pro.data.local.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "vertices",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["project_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("project_id")]
)
data class VertexEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "project_id") val projectId: String,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double = 0.0,
    val order: Int,
    @ColumnInfo(name = "gps_accuracy") val gpsAccuracy: Float? = null,
    val timestamp: Long = System.currentTimeMillis()
)
