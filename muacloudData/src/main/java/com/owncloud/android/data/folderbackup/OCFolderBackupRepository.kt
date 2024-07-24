
package com.owncloud.android.data.folderbackup

import com.owncloud.android.data.folderbackup.datasources.LocalFolderBackupDataSource
import com.owncloud.android.domain.camerauploads.FolderBackupRepository
import com.owncloud.android.domain.camerauploads.model.CameraUploadsConfiguration
import com.owncloud.android.domain.camerauploads.model.FolderBackUpConfiguration
import kotlinx.coroutines.flow.Flow

class OCFolderBackupRepository(
    private val localFolderBackupDataSource: LocalFolderBackupDataSource
) : FolderBackupRepository {

    override fun getCameraUploadsConfiguration(): CameraUploadsConfiguration? =
        localFolderBackupDataSource.getCameraUploadsConfiguration()

    override fun getFolderBackupConfigurationByNameAsFlow(name: String): Flow<FolderBackUpConfiguration?> =
        localFolderBackupDataSource.getFolderBackupConfigurationByNameAsFlow(name)

    override fun saveFolderBackupConfiguration(folderBackUpConfiguration: FolderBackUpConfiguration) {
        localFolderBackupDataSource.saveFolderBackupConfiguration(folderBackUpConfiguration)
    }

    override fun resetFolderBackupConfigurationByName(name: String) =
        localFolderBackupDataSource.resetFolderBackupConfigurationByName(name)

}
