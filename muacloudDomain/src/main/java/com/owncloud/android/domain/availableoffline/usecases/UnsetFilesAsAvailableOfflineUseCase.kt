
package com.owncloud.android.domain.availableoffline.usecases

import com.owncloud.android.domain.BaseUseCaseWithResult
import com.owncloud.android.domain.availableoffline.model.AvailableOfflineStatus
import com.owncloud.android.domain.files.FileRepository
import com.owncloud.android.domain.files.model.OCFile

class UnsetFilesAsAvailableOfflineUseCase(
    private val fileRepository: FileRepository,
) : BaseUseCaseWithResult<Unit, UnsetFilesAsAvailableOfflineUseCase.Params>() {

    override fun run(params: Params) {
        params.filesToUnsetAsAvailableOffline.forEach { fileToUnsetAsAvailableOffline ->


            if (fileToUnsetAsAvailableOffline.availableOfflineStatus == AvailableOfflineStatus.AVAILABLE_OFFLINE) {
                fileRepository.updateFileWithNewAvailableOfflineStatus(
                    ocFile = fileToUnsetAsAvailableOffline,
                    newAvailableOfflineStatus = AvailableOfflineStatus.NOT_AVAILABLE_OFFLINE,
                )
            }
        }
    }

    data class Params(
        val filesToUnsetAsAvailableOffline: List<OCFile>
    )
}
