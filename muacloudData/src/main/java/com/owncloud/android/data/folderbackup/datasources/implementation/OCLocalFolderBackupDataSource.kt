
package com.owncloud.android.data.folderbackup.datasources.implementation

import androidx.annotation.VisibleForTesting
import com.owncloud.android.data.folderbackup.datasources.LocalFolderBackupDataSource
import com.owncloud.android.data.folderbackup.db.FolderBackUpEntity
import com.owncloud.android.data.folderbackup.db.FolderBackupDao
import com.owncloud.android.domain.camerauploads.model.CameraUploadsConfiguration
import com.owncloud.android.domain.camerauploads.model.FolderBackUpConfiguration
import com.owncloud.android.domain.camerauploads.model.FolderBackUpConfiguration.Companion.pictureUploadsName
import com.owncloud.android.domain.camerauploads.model.FolderBackUpConfiguration.Companion.videoUploadsName
import com.owncloud.android.domain.camerauploads.model.UploadBehavior
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OCLocalFolderBackupDataSource(
    private val folderBackupDao: FolderBackupDao,
) : LocalFolderBackupDataSource {

    override fun getCameraUploadsConfiguration(): CameraUploadsConfiguration? {
        val pictureUploadsConfiguration = folderBackupDao.getFolderBackUpConfigurationByName(pictureUploadsName)
        val videoUploadsConfiguration = folderBackupDao.getFolderBackUpConfigurationByName(videoUploadsName)

        if (pictureUploadsConfiguration == null && videoUploadsConfiguration == null) return null

        return CameraUploadsConfiguration(
            pictureUploadsConfiguration = pictureUploadsConfiguration?.toModel(),
            videoUploadsConfiguration = videoUploadsConfiguration?.toModel(),
        )
    }

    override fun getFolderBackupConfigurationByNameAsFlow(name: String): Flow<FolderBackUpConfiguration?> =
        folderBackupDao.getFolderBackUpConfigurationByNameAsFlow(name = name).map { it?.toModel() }

    override fun saveFolderBackupConfiguration(folderBackUpConfiguration: FolderBackUpConfiguration) {
        folderBackupDao.update(folderBackUpConfiguration.toEntity())
    }

    override fun resetFolderBackupConfigurationByName(name: String) {
        folderBackupDao.delete(name)
    }

    
    companion object {
        @VisibleForTesting
        fun FolderBackUpEntity.toModel() =
            FolderBackUpConfiguration(
                accountName = accountName,
                behavior = UploadBehavior.fromString(behavior),
                sourcePath = sourcePath,
                uploadPath = uploadPath,
                wifiOnly = wifiOnly,
                chargingOnly = chargingOnly,
                lastSyncTimestamp = lastSyncTimestamp,
                name = name,
                spaceId = spaceId,
            )
    }


    private fun FolderBackUpConfiguration.toEntity(): FolderBackUpEntity =
        FolderBackUpEntity(
            accountName = accountName,
            behavior = behavior.toString(),
            sourcePath = sourcePath,
            uploadPath = uploadPath,
            wifiOnly = wifiOnly,
            chargingOnly = chargingOnly,
            name = name,
            lastSyncTimestamp = lastSyncTimestamp,
            spaceId = spaceId,
        )
}
