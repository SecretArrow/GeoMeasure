package com.geomeasure.pro.domain.usecase

import com.geomeasure.pro.data.local.db.AppDatabase
import com.geomeasure.pro.data.remote.drive.DriveManager
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import javax.inject.Inject

class SyncToDriveUseCase @Inject constructor(
    private val driveManager: DriveManager,
    private val db: AppDatabase
) {
    suspend operator fun invoke(
        account: GoogleSignInAccount,
        onProgress: (String) -> Unit
    ) = driveManager.syncAll(account, db, onProgress)
}
