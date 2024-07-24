
package com.owncloud.android.domain.camerauploads.usecases

import com.owncloud.android.domain.BaseUseCase
import com.owncloud.android.domain.camerauploads.FolderBackupRepository
import com.owncloud.android.domain.camerauploads.model.FolderBackUpConfiguration
import kotlinx.coroutines.flow.Flow

class GetPictureUploadsConfigurationStreamUseCase(
    private val folderBackupRepository: FolderBackupRepository
) : BaseUseCase<Flow<FolderBackUpConfiguration?>, Unit>() {

    override fun run(params: Unit): Flow<FolderBackUpConfiguration?> =
        folderBackupRepository.getFolderBackupConfigurationByNameAsFlow(FolderBackUpConfiguration.pictureUploadsName)
}
