package com.geomeasure.pro.data.local.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "projects",
    foreignKeys = [
        ForeignKey(
            entity = FolderEntity::class,
            parentColumns = ["id"],
            childColumns = ["folder_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ProjectEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val notes: String = "",
    @ColumnInfo(name = "folder_id") val folderId: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "modified_at") val modifiedAt: Long = System.currentTimeMillis(),
    val color: Int = 0xFF4CAF50.toInt(),
    val strokeColor: Int = 0xFF2E7D32.toInt(),
    @ColumnInfo(name = "measurement_type") val measurementType: String = "POLYGON",
    @ColumnInfo(name = "area_m2") val areaM2: Double = 0.0,
    @ColumnInfo(name = "perimeter_m") val perimeterM: Double = 0.0,
    @ColumnInfo(name = "is_synced") val isSynced: Boolean = false,
    @ColumnInfo(name = "drive_file_id") val driveFileId: String? = null
)
