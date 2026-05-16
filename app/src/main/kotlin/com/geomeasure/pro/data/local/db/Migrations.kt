package com.geomeasure.pro.data.local.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = Migration(1, 2) { db ->
    db.execSQL("ALTER TABLE projects ADD COLUMN is_synced INTEGER NOT NULL DEFAULT 0")
    db.execSQL("ALTER TABLE projects ADD COLUMN drive_file_id TEXT")
}

val MIGRATION_2_3 = Migration(2, 3) { db ->
    db.execSQL("ALTER TABLE projects ADD COLUMN stroke_color INTEGER NOT NULL DEFAULT -3866258")
}
