
package com.owncloud.android.data.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.owncloud.android.data.ProviderMeta.ProviderTableMeta.FOLDER_BACKUP_TABLE_NAME

val MIGRATION_34_35 = object : Migration(34, 35) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.run {
            execSQL("ALTER TABLE $FOLDER_BACKUP_TABLE_NAME ADD COLUMN chargingOnly INTEGER NOT NULL DEFAULT '0'")
        }
    }
}
