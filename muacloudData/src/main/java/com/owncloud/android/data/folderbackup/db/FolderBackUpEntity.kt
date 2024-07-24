
package com.owncloud.android.data.folderbackup.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.owncloud.android.data.ProviderMeta

@Entity(tableName = ProviderMeta.ProviderTableMeta.FOLDER_BACKUP_TABLE_NAME)
data class FolderBackUpEntity(
    val accountName: String,
    val behavior: String,
    val sourcePath: String,
    val uploadPath: String,
    val wifiOnly: Boolean,
    val chargingOnly: Boolean,
    val name: String,
    val lastSyncTimestamp: Long,
    val spaceId: String?,
) {
    @PrimaryKey(autoGenerate = true) var id: Int = 0
}
