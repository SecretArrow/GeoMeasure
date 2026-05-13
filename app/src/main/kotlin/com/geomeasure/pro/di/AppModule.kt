package com.geomeasure.pro.di

import android.content.Context
import android.location.LocationManager
import com.geomeasure.pro.core.security.KeystoreManager
import com.geomeasure.pro.data.local.db.AppDatabase
import com.geomeasure.pro.data.local.db.dao.FolderDao
import com.geomeasure.pro.data.local.db.dao.ProjectDao
import com.geomeasure.pro.data.local.db.dao.VertexDao
import com.geomeasure.pro.data.local.prefs.AppPreferences
import com.geomeasure.pro.data.remote.drive.DriveManager
import com.geomeasure.pro.data.repository.MeasurementRepositoryImpl
import com.geomeasure.pro.domain.repository.MeasurementRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
        AppDatabase.getInstance(ctx, KeystoreManager.getOrCreateDatabaseKey(ctx))

    @Provides
    @Singleton
    fun provideProjectDao(db: AppDatabase): ProjectDao = db.projectDao()

    @Provides
    @Singleton
    fun provideVertexDao(db: AppDatabase): VertexDao = db.vertexDao()

    @Provides
    @Singleton
    fun provideFolderDao(db: AppDatabase): FolderDao = db.folderDao()

    @Provides
    @Singleton
    fun provideMeasurementRepository(
        projectDao: ProjectDao,
        vertexDao: VertexDao,
        folderDao: FolderDao
    ): MeasurementRepository =
        MeasurementRepositoryImpl(projectDao, vertexDao, folderDao)

    @Provides
    @Singleton
    fun provideMeasurementRepositoryImpl(
        projectDao: ProjectDao,
        vertexDao: VertexDao,
        folderDao: FolderDao
    ): MeasurementRepositoryImpl =
        MeasurementRepositoryImpl(projectDao, vertexDao, folderDao)

    @Provides
    @Singleton
    fun provideDriveManager(@ApplicationContext ctx: Context) = DriveManager(ctx)

    @Provides
    @Singleton
    fun providePreferences(@ApplicationContext ctx: Context): AppPreferences =
        AppPreferences(ctx)

    @Provides
    @Singleton
    fun provideLocationManager(@ApplicationContext ctx: Context): LocationManager =
        ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
}
