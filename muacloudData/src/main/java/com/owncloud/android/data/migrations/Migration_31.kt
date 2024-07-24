
package com.owncloud.android.data.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_30_31 = object : Migration(30, 31) {
    override fun migrate(database: SupportSQLiteDatabase) {
        //Nothing to migrate at the moment
    }
}
