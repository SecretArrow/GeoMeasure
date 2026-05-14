package com.geomeasure.pro.data.local.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.geomeasure.pro.data.local.db.dao.FolderDao
import com.geomeasure.pro.data.local.db.dao.ProjectDao
import com.geomeasure.pro.data.local.db.dao.VertexDao
import com.geomeasure.pro.data.local.db.entities.FolderEntity
import com.geomeasure.pro.data.local.db.entities.ProjectEntity
import com.geomeasure.pro.data.local.db.entities.VertexEntity
import net.sqlcipher.database.SupportFactory

@Database(
    entities = [ProjectEntity::class, VertexEntity::class, FolderEntity::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun vertexDao(): VertexDao
    abstract fun folderDao(): FolderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: android.content.Context, passphrase: ByteArray): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val factory = SupportFactory(passphrase)
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "geomeasure.db"
                )
                    .openHelperFactory(factory)
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
