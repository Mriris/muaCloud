
package com.owncloud.android.domain.camerauploads.usecases

import com.owncloud.android.domain.BaseUseCaseWithResult
import com.owncloud.android.domain.camerauploads.FolderBackupRepository
import com.owncloud.android.domain.camerauploads.model.CameraUploadsConfiguration

class GetCameraUploadsConfigurationUseCase(
    private val folderBackupRepository: FolderBackupRepository
) : BaseUseCaseWithResult<CameraUploadsConfiguration?, Unit>() {

    override fun run(params: Unit): CameraUploadsConfiguration? =
        folderBackupRepository.getCameraUploadsConfiguration()
}
