

package com.owncloud.android.data.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_41_42 = object : Migration(41, 42) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.run {
            execSQL("ALTER TABLE `files` ADD COLUMN `lastUsage` INTEGER")

            execSQL("UPDATE `files` SET `lastUsage` = CASE WHEN `storagePath` IS NOT NULL THEN ${System.currentTimeMillis()} ELSE NULL END")
        }
    }
}
