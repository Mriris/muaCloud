
package com.owncloud.android.domain.camerauploads.usecases

import com.owncloud.android.domain.BaseUseCaseWithResult
import com.owncloud.android.domain.camerauploads.FolderBackupRepository
import com.owncloud.android.domain.camerauploads.model.FolderBackUpConfiguration

class SavePictureUploadsConfigurationUseCase(
    private val folderBackupRepository: FolderBackupRepository
) : BaseUseCaseWithResult<Unit, SavePictureUploadsConfigurationUseCase.Params>() {

    override fun run(params: Params) =
        folderBackupRepository.saveFolderBackupConfiguration(params.pictureUploadsConfiguration)

    data class Params(
        val pictureUploadsConfiguration: FolderBackUpConfiguration
    )
}
