package com.geomeasure.pro.data.local.db.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.geomeasure.pro.data.local.db.AppDatabase
import com.geomeasure.pro.data.local.db.entities.ProjectEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import net.sqlcipher.database.SupportFactory
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class ProjectDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var dao: ProjectDao
    private val passphrase = "test-key".toByteArray()

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "test_geomeasure.db"
        )
            .openHelperFactory(SupportFactory(passphrase))
            .build()
        dao = db.projectDao()
    }

    @After
    fun teardown() {
        db.close()
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.getDatabasePath("test_geomeasure.db").delete()
    }

    @Test
    fun insertAndReadProject() = runBlocking {
        val project = ProjectEntity(name = "Test Project", notes = "Test notes")
        dao.insertProject(project)

        val projects = dao.getAllProjects().first()
        assert(projects.any { it.id == project.id }) { "Project should be found" }
    }

    @Test
    fun deleteProject() = runBlocking {
        val project = ProjectEntity(name = "To Delete")
        dao.insertProject(project)
        dao.deleteProject(project)

        val projects = dao.getAllProjects().first()
        assert(projects.none { it.id == project.id }) { "Project should be deleted" }
    }

    @Test
    fun searchProjects() = runBlocking {
        dao.insertProject(ProjectEntity(name = "Alpha Beta"))
        dao.insertProject(ProjectEntity(name = "Gamma Delta"))

        val results = dao.searchProjects("Alpha").first()
        assert(results.size >= 1) { "Should find at least one project" }
    }

    @Test
    fun updateProject() = runBlocking {
        val project = ProjectEntity(name = "Original")
        dao.insertProject(project)
        dao.updateProject(project.copy(name = "Updated"))

        val updated = dao.getProjectById(project.id)
        assert(updated?.name == "Updated") { "Name should be updated" }
    }

    @Test
    fun getUnsyncedProjects() = runBlocking {
        dao.insertProject(ProjectEntity(name = "Unsynced 1", isSynced = false))
        dao.insertProject(ProjectEntity(name = "Synced", isSynced = true))

        val unsynced = dao.getUnsyncedProjects()
        assert(unsynced.all { !it.isSynced }) { "All returned should be unsynced" }
    }
}
