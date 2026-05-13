package com.geomeasure.pro.data.local.db.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.geomeasure.pro.data.local.db.AppDatabase
import com.geomeasure.pro.data.local.db.entities.ProjectEntity
import com.geomeasure.pro.data.local.db.entities.VertexEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import net.sqlcipher.database.SupportFactory
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VertexDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var projectDao: ProjectDao
    private lateinit var vertexDao: VertexDao
    private val passphrase = "test-key".toByteArray()
    private lateinit var testProjectId: String

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "test_vertices.db"
        )
            .openHelperFactory(SupportFactory(passphrase))
            .build()
        projectDao = db.projectDao()
        vertexDao = db.vertexDao()

        runBlocking {
            val project = ProjectEntity(name = "Test")
            projectDao.insertProject(project)
            testProjectId = project.id
        }
    }

    @After
    fun teardown() {
        db.close()
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.getDatabasePath("test_vertices.db").delete()
    }

    @Test
    fun insertAndGetVertices() = runBlocking {
        val vertex = VertexEntity(
            projectId = testProjectId,
            latitude = -6.2,
            longitude = 106.8,
            order = 0
        )
        vertexDao.insertVertex(vertex)

        val vertices = vertexDao.getVerticesForProjectFlow(testProjectId).first()
        assert(vertices.any { it.id == vertex.id }) { "Vertex should be found" }
        assert(vertices.size == 1) { "Should have 1 vertex" }
    }

    @Test
    fun verticesOrderedByOrder() = runBlocking {
        vertexDao.insertVertex(VertexEntity(projectId = testProjectId, latitude = 1.0, longitude = 1.0, order = 2))
        vertexDao.insertVertex(VertexEntity(projectId = testProjectId, latitude = 0.0, longitude = 0.0, order = 0))
        vertexDao.insertVertex(VertexEntity(projectId = testProjectId, latitude = 0.0, longitude = 1.0, order = 1))

        val vertices = vertexDao.getVerticesForProjectFlow(testProjectId).first()
        assert(vertices[0].order == 0) { "First should have order 0" }
        assert(vertices[1].order == 1) { "Second should have order 1" }
        assert(vertices[2].order == 2) { "Third should have order 2" }
    }

    @Test
    fun deleteCascadeWithProject() = runBlocking {
        vertexDao.insertVertex(VertexEntity(projectId = testProjectId, latitude = 0.0, longitude = 0.0, order = 0))
        projectDao.deleteById(testProjectId)

        val vertices = vertexDao.getVerticesForProjectFlow(testProjectId).first()
        assert(vertices.isEmpty()) { "Vertices should be cascade deleted" }
    }
}
