package com.geomeasure.pro.data.remote.drive

import android.content.Context
import android.content.Intent
import com.geomeasure.pro.data.export.GeoJsonExporter
import com.geomeasure.pro.data.local.db.AppDatabase
import com.geomeasure.pro.data.local.db.entities.ProjectEntity
import com.geomeasure.pro.data.local.db.entities.VertexEntity
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DriveManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val DRIVE_FOLDER_NAME = "GeoMeasure Pro Backups"
        const val MIME_GEOJSON = "application/geo+json"
        const val MIME_FOLDER = "application/vnd.google-apps.folder"
        val SCOPES = listOf(DriveScopes.DRIVE_FILE)
    }

    private val googleSignInClient by lazy {
        val options = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(
            com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
        )
            .requestEmail()
            .requestScopes(com.google.android.gms.common.api.Scope(DriveScopes.DRIVE_FILE))
            .build()
        com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(context, options)
    }

    fun getSignInIntent(): Intent = googleSignInClient.signInIntent

    suspend fun handleSignInResult(data: Intent?): com.google.android.gms.auth.api.signin.GoogleSignInAccount? =
        withContext(Dispatchers.IO) {
            try {
                val task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(data)
                task.result
            } catch (_: Exception) { null }
        }

    private fun buildDriveService(account: com.google.android.gms.auth.api.signin.GoogleSignInAccount): Drive? {
        return try {
            val credential = GoogleAccountCredential.usingOAuth2(context, SCOPES)
            credential.selectedAccount = account.account ?: return null
            Drive.Builder(
                NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                credential
            ).setApplicationName("GeoMeasure Pro").build()
        } catch (_: Exception) { null }
    }

    suspend fun uploadProject(
        account: com.google.android.gms.auth.api.signin.GoogleSignInAccount,
        project: ProjectEntity,
        vertices: List<VertexEntity>
    ): String? = withContext(Dispatchers.IO) {
        try {
            val drive = buildDriveService(account) ?: return@withContext null
            val folderId = getOrCreateAppFolder(drive) ?: return@withContext null
            val content = GeoJsonExporter().export(project, vertices)
            val fileContent = com.google.api.client.http.ByteArrayContent.fromString(MIME_GEOJSON, content)

            val existingId = project.driveFileId
            if (existingId != null) {
                drive.files().update(existingId, null, fileContent).execute()
                existingId
            } else {
                val metadata = File().apply {
                    name = "${project.name}.geojson"
                    parents = listOf(folderId)
                    mimeType = MIME_GEOJSON
                }
                drive.files().create(metadata, fileContent)
                    .setFields("id").execute().id
            }
        } catch (e: Exception) {
            android.util.Log.e("DriveManager", "Upload failed", e)
            null
        }
    }

    suspend fun downloadAll(account: com.google.android.gms.auth.api.signin.GoogleSignInAccount): List<Pair<String, String>> =
        withContext(Dispatchers.IO) {
            try {
                val drive = buildDriveService(account) ?: return@withContext emptyList()
                val folderId = getOrCreateAppFolder(drive) ?: return@withContext emptyList()
                drive.files().list()
                    .setQ("'$folderId' in parents and mimeType='$MIME_GEOJSON' and trashed=false")
                    .setFields("files(id, name)")
                    .execute().files
                    .mapNotNull { file ->
                        try {
                            val content = drive.files().get(file.id).executeMediaAsInputStream().use { stream ->
                                stream.bufferedReader().readText()
                            }
                            Pair(file.name, content)
                        } catch (_: Exception) { null }
                    }
            } catch (e: Exception) {
                android.util.Log.e("DriveManager", "DownloadAll failed", e)
                emptyList()
            }
        }

    suspend fun deleteFile(account: com.google.android.gms.auth.api.signin.GoogleSignInAccount, fileId: String) {
        try {
            withContext(Dispatchers.IO) {
                val drive = buildDriveService(account) ?: return@withContext
                drive.files().delete(fileId).execute()
            }
        } catch (e: Exception) {
            android.util.Log.e("DriveManager", "Delete failed", e)
        }
    }

    suspend fun syncAll(
        account: com.google.android.gms.auth.api.signin.GoogleSignInAccount,
        db: AppDatabase,
        onProgress: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val unsynced = db.projectDao().getUnsyncedProjects()
            unsynced.forEach { project ->
                onProgress("Syncing: ${project.name}")
                val vertices = db.vertexDao().getVerticesForProject(project.id)
                val fileId = uploadProject(account, project, vertices)
                if (fileId != null) {
                    db.projectDao().updateProject(project.copy(isSynced = true, driveFileId = fileId))
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("DriveManager", "SyncAll failed", e)
        }
    }

    private suspend fun getOrCreateAppFolder(drive: Drive): String? =
        withContext(Dispatchers.IO) {
            try {
                val result = drive.files().list()
                    .setQ("mimeType='$MIME_FOLDER' and name='$DRIVE_FOLDER_NAME' and trashed=false")
                    .setFields("files(id)")
                    .execute()
                result.files.firstOrNull()?.id ?: run {
                    val folder = File().apply {
                        name = DRIVE_FOLDER_NAME
                        mimeType = MIME_FOLDER
                    }
                    drive.files().create(folder).setFields("id").execute().id
                }
            } catch (e: Exception) {
                android.util.Log.e("DriveManager", "getOrCreateAppFolder failed", e)
                null
            }
        }
}
