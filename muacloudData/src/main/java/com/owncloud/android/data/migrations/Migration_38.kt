

package com.owncloud.android.data.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_37_38 = object : Migration(37, 38) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS `files` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `parentId` INTEGER, `owner` TEXT NOT NULL, `remotePath` TEXT NOT NULL, `remoteId` TEXT, `length` INTEGER NOT NULL, `creationTimestamp` INTEGER, `modificationTimestamp` INTEGER NOT NULL, `mimeType` TEXT NOT NULL, `etag` TEXT, `permissions` TEXT, `privateLink` TEXT, `storagePath` TEXT, `name` TEXT, `treeEtag` TEXT, `keepInSync` INTEGER, `lastSyncDateForData` INTEGER, `fileShareViaLink` INTEGER, `lastSyncDateForProperties` INTEGER, `needsToUpdateThumbnail` INTEGER NOT NULL, `modifiedAtLastSyncForData` INTEGER, `etagInConflict` TEXT, `fileIsDownloading` INTEGER, `sharedWithSharee` INTEGER, `sharedByLink` INTEGER NOT NULL)")
        database.execSQL("CREATE TABLE IF NOT EXISTS `transfers` (`localPath` TEXT NOT NULL, `remotePath` TEXT NOT NULL, `accountName` TEXT NOT NULL, `fileSize` INTEGER NOT NULL, `status` INTEGER NOT NULL, `localBehaviour` INTEGER NOT NULL, `forceOverwrite` INTEGER NOT NULL, `transferEndTimestamp` INTEGER, `lastResult` INTEGER, `createdBy` INTEGER NOT NULL, `transferId` TEXT, `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL)")
    }
}
