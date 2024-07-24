
package com.owncloud.android.data.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_31_32 = object : Migration(31, 32) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS `user_quotas` (`accountName` TEXT NOT NULL, `used` INTEGER NOT NULL, `available` INTEGER NOT NULL, PRIMARY KEY(`accountName`))")
    }
}
