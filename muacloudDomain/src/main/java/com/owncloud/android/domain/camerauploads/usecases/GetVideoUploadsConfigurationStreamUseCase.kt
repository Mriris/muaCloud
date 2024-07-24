
package com.owncloud.android.domain.camerauploads.usecases

import com.owncloud.android.domain.BaseUseCase
import com.owncloud.android.domain.camerauploads.FolderBackupRepository
import com.owncloud.android.domain.camerauploads.model.FolderBackUpConfiguration
import com.owncloud.android.domain.camerauploads.model.FolderBackUpConfiguration.Companion.videoUploadsName
import kotlinx.coroutines.flow.Flow

class GetVideoUploadsConfigurationStreamUseCase(
    private val folderBackupRepository: FolderBackupRepository
) : BaseUseCase<Flow<FolderBackUpConfiguration?>, Unit>() {

    override fun run(params: Unit): Flow<FolderBackUpConfiguration?> =
        folderBackupRepository.getFolderBackupConfigurationByNameAsFlow(videoUploadsName)
}
