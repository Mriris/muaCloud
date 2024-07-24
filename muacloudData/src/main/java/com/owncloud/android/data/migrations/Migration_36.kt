

package com.owncloud.android.data.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.owncloud.android.data.ProviderMeta.ProviderTableMeta.CAPABILITIES_TABLE_NAME

val MIGRATION_35_36 = object : Migration(35, 36) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.run {
            execSQL("ALTER TABLE $CAPABILITIES_TABLE_NAME ADD COLUMN sharing_user_profile_picture INTEGER NOT NULL DEFAULT -1")
        }
    }
}
