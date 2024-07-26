
package com.owncloud.android.data.migrations

import android.content.ContentValues
import android.database.sqlite.SQLiteException
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.owncloud.android.data.ProviderMeta.ProviderTableMeta.OCSHARES_TABLE_NAME
import timber.log.Timber

val MIGRATION_32_33 = object : Migration(32, 33) {
    override fun migrate(database: SupportSQLiteDatabase) {

        try {

            database.execSQL("CREATE TABLE IF NOT EXISTS `${OCSHARES_TABLE_NAME}2` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `share_type` INTEGER NOT NULL, `share_with` TEXT, `path` TEXT NOT NULL, `permissions` INTEGER NOT NULL, `shared_date` INTEGER NOT NULL, `expiration_date` INTEGER NOT NULL, `token` TEXT, `shared_with_display_name` TEXT, `share_with_additional_info` TEXT, `is_directory` INTEGER NOT NULL, `id_remote_shared` TEXT NOT NULL, `owner_share` TEXT NOT NULL, `name` TEXT, `url` TEXT)")

            val cursor = database.query("SELECT * FROM $OCSHARES_TABLE_NAME")
            cursor.use {
                while (it.moveToNext()) {
                    val cv = ContentValues()
                    cv.put("id", it.getInt(it.getColumnIndexOrThrow("id")))
                    cv.put("share_type", it.getInt(it.getColumnIndexOrThrow("share_type")))
                    cv.put("share_with", it.getString(it.getColumnIndexOrThrow("shate_with")))
                    cv.put("path", it.getString(it.getColumnIndexOrThrow("path")))
                    cv.put("permissions", it.getInt(it.getColumnIndexOrThrow("permissions")))
                    cv.put("shared_date", it.getInt(it.getColumnIndexOrThrow("shared_date")))
                    cv.put("expiration_date", it.getInt(it.getColumnIndexOrThrow("expiration_date")))
                    cv.put("token", it.getString(it.getColumnIndexOrThrow("token")))
                    cv.put("shared_with_display_name", it.getString(it.getColumnIndexOrThrow("shared_with_display_name")))
                    cv.put("share_with_additional_info", it.getString(it.getColumnIndexOrThrow("share_with_additional_info")))
                    cv.put("is_directory", it.getInt(it.getColumnIndexOrThrow("is_directory")))
                    cv.put("id_remote_shared", it.getString(it.getColumnIndexOrThrow("id_remote_shared")))
                    cv.put("owner_share", it.getString(it.getColumnIndexOrThrow("owner_share")))
                    cv.put("name", it.getString(it.getColumnIndexOrThrow("name")))
                    cv.put("url", it.getString(it.getColumnIndexOrThrow("url")))

                    database.insert("${OCSHARES_TABLE_NAME}2", 0, cv)
                }
            }

            database.execSQL("DROP TABLE $OCSHARES_TABLE_NAME")
            database.execSQL("ALTER TABLE ${OCSHARES_TABLE_NAME}2 RENAME TO $OCSHARES_TABLE_NAME")

        } catch (e: SQLiteException) {
            Timber.e(e, "SQLiteException in migrate from database version 32 to version 33")
        } catch (e: Exception) {
            Timber.e(e, "Failed to migrate database version 32 to version 33")
        }
    }
}
