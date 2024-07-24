
package com.owncloud.android.data.folderbackup.datasources

import com.owncloud.android.domain.camerauploads.model.CameraUploadsConfiguration
import com.owncloud.android.domain.camerauploads.model.FolderBackUpConfiguration
import kotlinx.coroutines.flow.Flow

interface LocalFolderBackupDataSource {
    fun getCameraUploadsConfiguration(): CameraUploadsConfiguration?

    fun getFolderBackupConfigurationByNameAsFlow(name: String): Flow<FolderBackUpConfiguration?>

    fun saveFolderBackupConfiguration(folderBackUpConfiguration: FolderBackUpConfiguration)

    fun resetFolderBackupConfigurationByName(name: String)
}
