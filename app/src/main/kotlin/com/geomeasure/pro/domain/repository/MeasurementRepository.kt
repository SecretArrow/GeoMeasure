package com.geomeasure.pro.domain.repository

import com.geomeasure.pro.data.local.db.entities.FolderEntity
import com.geomeasure.pro.data.local.db.entities.ProjectEntity
import com.geomeasure.pro.data.local.db.entities.VertexEntity
import kotlinx.coroutines.flow.Flow

interface MeasurementRepository {
    fun getAllProjects(): Flow<List<ProjectEntity>>
    fun getProjectsByFolder(folderId: String?): Flow<List<ProjectEntity>>
    suspend fun getProjectById(id: String): ProjectEntity?
    fun searchProjects(query: String): Flow<List<ProjectEntity>>
    suspend fun insertProject(project: ProjectEntity)
    suspend fun updateProject(project: ProjectEntity)
    suspend fun deleteProject(project: ProjectEntity)
    suspend fun deleteProjectById(id: String)
    fun getVerticesForProject(projectId: String): Flow<List<VertexEntity>>
    suspend fun getVerticesForProjectList(projectId: String): List<VertexEntity>
    suspend fun insertVertex(vertex: VertexEntity)
    suspend fun insertVertices(vertices: List<VertexEntity>)
    suspend fun updateVertex(vertex: VertexEntity)
    suspend fun deleteVertex(vertex: VertexEntity)
    suspend fun addVertex(projectId: String, location: android.location.Location)
    fun getAllFolders(): Flow<List<FolderEntity>>
    suspend fun insertFolder(folder: FolderEntity)
}
