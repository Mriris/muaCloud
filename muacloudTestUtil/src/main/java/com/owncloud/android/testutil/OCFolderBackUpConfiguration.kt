
package com.owncloud.android.testutil

import com.owncloud.android.domain.camerauploads.model.FolderBackUpConfiguration
import com.owncloud.android.domain.camerauploads.model.UploadBehavior
import com.owncloud.android.data.folderbackup.db.FolderBackUpEntity

val OC_BACKUP = FolderBackUpConfiguration(
    accountName = "",
    behavior = UploadBehavior.COPY,
    sourcePath = "/Photos",
    uploadPath = "/Photos",
    wifiOnly = true,
    chargingOnly = true,
    lastSyncTimestamp = 1542628397,
    name = "",
    spaceId = null,
)

val OC_BACKUP_ENTITY = FolderBackUpEntity(
    accountName = "",
    behavior = UploadBehavior.COPY.name,
    sourcePath = "/Photos",
    uploadPath = "/Photos",
    wifiOnly = true,
    chargingOnly = true,
    lastSyncTimestamp = 1542628397,
    name = "",
    spaceId = null,
)
