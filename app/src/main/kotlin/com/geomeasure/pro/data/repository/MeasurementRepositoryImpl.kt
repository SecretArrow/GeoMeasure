package com.geomeasure.pro.data.repository

import com.geomeasure.pro.data.local.db.dao.FolderDao
import com.geomeasure.pro.data.local.db.dao.ProjectDao
import com.geomeasure.pro.data.local.db.dao.VertexDao
import com.geomeasure.pro.data.local.db.entities.FolderEntity
import com.geomeasure.pro.data.local.db.entities.ProjectEntity
import com.geomeasure.pro.data.local.db.entities.VertexEntity
import com.geomeasure.pro.domain.repository.MeasurementRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MeasurementRepositoryImpl @Inject constructor(
    private val projectDao: ProjectDao,
    private val vertexDao: VertexDao,
    private val folderDao: FolderDao
) : MeasurementRepository {

    override fun getAllProjects(): Flow<List<ProjectEntity>> = projectDao.getAllProjects()

    override fun getProjectsByFolder(folderId: String?): Flow<List<ProjectEntity>> =
        projectDao.getProjectsByFolder(folderId)

    override suspend fun getProjectById(id: String): ProjectEntity? =
        projectDao.getProjectById(id)

    override fun searchProjects(query: String): Flow<List<ProjectEntity>> =
        projectDao.searchProjects(query)

    override suspend fun insertProject(project: ProjectEntity) =
        projectDao.insertProject(project)

    override suspend fun updateProject(project: ProjectEntity) =
        projectDao.updateProject(project)

    override suspend fun deleteProject(project: ProjectEntity) =
        projectDao.deleteProject(project)

    override suspend fun deleteProjectById(id: String) =
        projectDao.deleteById(id)

    override fun getVerticesForProject(projectId: String): Flow<List<VertexEntity>> =
        vertexDao.getVerticesForProjectFlow(projectId)

    override suspend fun getVerticesForProjectList(projectId: String): List<VertexEntity> =
        vertexDao.getVerticesForProject(projectId)

    override suspend fun insertVertex(vertex: VertexEntity) =
        vertexDao.insertVertex(vertex)

    override suspend fun insertVertices(vertices: List<VertexEntity>) =
        vertexDao.insertVertices(vertices)

    override suspend fun updateVertex(vertex: VertexEntity) =
        vertexDao.updateVertex(vertex)

    override suspend fun deleteVertex(vertex: VertexEntity) =
        vertexDao.deleteVertex(vertex)

    override suspend fun addVertex(projectId: String, location: android.location.Location) {
        val vertices = vertexDao.getVerticesForProject(projectId)
        val order = vertices.size
        val vertex = VertexEntity(
            projectId = projectId,
            latitude = location.latitude,
            longitude = location.longitude,
            altitude = location.altitude,
            order = order,
            gpsAccuracy = location.accuracy
        )
        vertexDao.insertVertex(vertex)
    }

    override fun getAllFolders(): Flow<List<FolderEntity>> = folderDao.getAllFolders()

    override suspend fun insertFolder(folder: FolderEntity) =
        folderDao.insertFolder(folder)
}
