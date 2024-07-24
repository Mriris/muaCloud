
package com.owncloud.android.domain.camerauploads

import com.owncloud.android.domain.camerauploads.model.CameraUploadsConfiguration
import com.owncloud.android.domain.camerauploads.model.FolderBackUpConfiguration
import kotlinx.coroutines.flow.Flow

interface FolderBackupRepository {
    fun getCameraUploadsConfiguration(): CameraUploadsConfiguration?

    fun getFolderBackupConfigurationByNameAsFlow(name: String): Flow<FolderBackUpConfiguration?>

    fun saveFolderBackupConfiguration(folderBackUpConfiguration: FolderBackUpConfiguration)

    fun resetFolderBackupConfigurationByName(name: String)
}
