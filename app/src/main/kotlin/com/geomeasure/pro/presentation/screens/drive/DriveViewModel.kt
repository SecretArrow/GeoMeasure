package com.geomeasure.pro.presentation.screens.drive

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.geomeasure.pro.data.remote.drive.DriveManager
import com.geomeasure.pro.domain.usecase.SyncToDriveUseCase
import com.google.android.gms.auth.api.signin.GoogleSignIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DriveUiState(
    val isSignedIn: Boolean = false,
    val accountEmail: String? = null,
    val isSyncing: Boolean = false,
    val syncProgress: String = "",
    val lastSyncTime: Long? = null,
    val syncCount: Int = 0,
    val error: String? = null
)

@HiltViewModel
class DriveViewModel @Inject constructor(
    application: Application,
    private val driveManager: DriveManager,
    private val syncToDrive: SyncToDriveUseCase
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(DriveUiState())
    val uiState: StateFlow<DriveUiState> = _uiState.asStateFlow()

    init {
        checkSignInStatus()
    }

    private fun checkSignInStatus() {
        val account = GoogleSignIn.getLastSignedInAccount(getApplication())
        _uiState.update {
            it.copy(
                isSignedIn = account != null,
                accountEmail = account?.email
            )
        }
    }

    fun signIn() {
        // Triggers sign-in intent; handled by Activity result
    }

    fun handleSignInResult(data: Intent?) {
        viewModelScope.launch {
            val account = driveManager.handleSignInResult(data)
            if (account != null) {
                _uiState.update {
                    it.copy(isSignedIn = true, accountEmail = account.email, error = null)
                }
            } else {
                _uiState.update { it.copy(error = "Sign in failed") }
            }
        }
    }

    fun syncAll() {
        val account = GoogleSignIn.getLastSignedInAccount(getApplication()) ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, syncProgress = "Starting sync...", error = null) }
            try {
                var count = 0
                syncToDrive(account) { progress ->
                    _uiState.update { it.copy(syncProgress = progress) }
                    count++
                }
                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        syncProgress = "Sync complete",
                        lastSyncTime = System.currentTimeMillis(),
                        syncCount = count
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSyncing = false, error = "Sync failed: ${e.message}")
                }
            }
        }
    }

    fun signOut() {
        val context = getApplication<Application>()
        viewModelScope.launch {
            GoogleSignIn.getClient(context, com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN)
                .signOut()
                .addOnCompleteListener {
                    _uiState.update {
                        it.copy(isSignedIn = false, accountEmail = null, syncCount = 0, lastSyncTime = null)
                    }
                }
        }
    }

    fun exportLocalBackup() {
        // Export to local file - triggered via UI
    }

    fun importLocalBackup() {
        // Import from local file - triggered via UI
    }
}
