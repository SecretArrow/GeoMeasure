package com.geomeasure.pro.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.geomeasure.pro.data.local.db.entities.VertexEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VertexDao {
    @Query("SELECT * FROM vertices WHERE project_id = :projectId ORDER BY `order` ASC")
    fun getVerticesForProjectFlow(projectId: String): Flow<List<VertexEntity>>

    @Query("SELECT * FROM vertices WHERE project_id = :projectId ORDER BY `order` ASC")
    suspend fun getVerticesForProject(projectId: String): List<VertexEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVertex(vertex: VertexEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVertices(vertices: List<VertexEntity>)

    @Update
    suspend fun updateVertex(vertex: VertexEntity)

    @Delete
    suspend fun deleteVertex(vertex: VertexEntity)

    @Query("DELETE FROM vertices WHERE project_id = :projectId")
    suspend fun deleteVerticesForProject(projectId: String)
}
