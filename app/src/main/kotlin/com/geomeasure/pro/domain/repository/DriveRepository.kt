package com.geomeasure.pro.domain.repository

interface DriveRepository {
    suspend fun isSignedIn(): Boolean
    suspend fun signOut()
}
