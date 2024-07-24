
package com.owncloud.android.domain.camerauploads.usecases

import com.owncloud.android.domain.BaseUseCase
import com.owncloud.android.domain.camerauploads.FolderBackupRepository
import com.owncloud.android.domain.camerauploads.model.FolderBackUpConfiguration.Companion.videoUploadsName

class ResetVideoUploadsUseCase(
    private val folderBackupRepository: FolderBackupRepository
) : BaseUseCase<Unit, Unit>() {

    override fun run(params: Unit) =
        folderBackupRepository.resetFolderBackupConfigurationByName(videoUploadsName)
}
