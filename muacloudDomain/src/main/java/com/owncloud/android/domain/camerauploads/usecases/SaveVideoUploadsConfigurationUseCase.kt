
package com.owncloud.android.domain.camerauploads.usecases

import com.owncloud.android.domain.BaseUseCaseWithResult
import com.owncloud.android.domain.camerauploads.FolderBackupRepository
import com.owncloud.android.domain.camerauploads.model.FolderBackUpConfiguration

class SaveVideoUploadsConfigurationUseCase(
    private val folderBackupRepository: FolderBackupRepository
) : BaseUseCaseWithResult<Unit, SaveVideoUploadsConfigurationUseCase.Params>() {

    override fun run(params: Params) =
        folderBackupRepository.saveFolderBackupConfiguration(params.videoUploadsConfiguration)

    data class Params(
        val videoUploadsConfiguration: FolderBackUpConfiguration
    )
}
