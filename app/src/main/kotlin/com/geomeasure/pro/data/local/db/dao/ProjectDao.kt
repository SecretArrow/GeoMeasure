package com.geomeasure.pro.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.geomeasure.pro.data.local.db.entities.ProjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY modified_at DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE folder_id = :folderId ORDER BY modified_at DESC")
    fun getProjectsByFolder(folderId: String?): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE folder_id IS NULL ORDER BY modified_at DESC")
    fun getRootProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getProjectById(id: String): ProjectEntity?

    @Query("SELECT * FROM projects WHERE name LIKE '%' || :query || '%'")
    fun searchProjects(query: String): Flow<List<ProjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity)

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Delete
    suspend fun deleteProject(project: ProjectEntity)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM projects WHERE is_synced = 0")
    suspend fun getUnsyncedProjects(): List<ProjectEntity>
}
