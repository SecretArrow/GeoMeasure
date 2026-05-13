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
import com.google.gson.Gson
import com.google.gson.JsonParser
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

    private fun buildDriveService(account: com.google.android.gms.auth.api.signin.GoogleSignInAccount): Drive {
        val credential = GoogleAccountCredential.usingOAuth2(context, SCOPES)
        credential.selectedAccount = account.account ?: throw IllegalStateException("No account")
        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        ).setApplicationName("GeoMeasure Pro").build()
    }

    suspend fun uploadProject(
        account: com.google.android.gms.auth.api.signin.GoogleSignInAccount,
        project: ProjectEntity,
        vertices: List<VertexEntity>
    ): String = withContext(Dispatchers.IO) {
        val drive = buildDriveService(account)
        val folderId = getOrCreateAppFolder(drive)
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
    }

    suspend fun downloadAll(account: com.google.android.gms.auth.api.signin.GoogleSignInAccount): List<Pair<String, String>> =
        withContext(Dispatchers.IO) {
            val drive = buildDriveService(account)
            val folderId = getOrCreateAppFolder(drive)
            drive.files().list()
                .setQ("'$folderId' in parents and mimeType='$MIME_GEOJSON' and trashed=false")
                .setFields("files(id, name)")
                .execute().files
                .map { file ->
                    val content = drive.files().get(file.id).executeMediaAsInputStream()
                        .bufferedReader().readText()
                    Pair(file.name, content)
                }
        }

    suspend fun deleteFile(account: com.google.android.gms.auth.api.signin.GoogleSignInAccount, fileId: String) =
        withContext(Dispatchers.IO) {
            buildDriveService(account).files().delete(fileId).execute()
        }

    suspend fun syncAll(
        account: com.google.android.gms.auth.api.signin.GoogleSignInAccount,
        db: AppDatabase,
        onProgress: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        val unsynced = db.projectDao().getUnsyncedProjects()
        unsynced.forEach { project ->
            onProgress("Syncing: ${project.name}")
            val vertices = db.vertexDao().getVerticesForProject(project.id)
            val fileId = uploadProject(account, project, vertices)
            db.projectDao().updateProject(project.copy(isSynced = true, driveFileId = fileId))
        }
    }

    private suspend fun getOrCreateAppFolder(drive: Drive): String =
        withContext(Dispatchers.IO) {
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
        }
}
