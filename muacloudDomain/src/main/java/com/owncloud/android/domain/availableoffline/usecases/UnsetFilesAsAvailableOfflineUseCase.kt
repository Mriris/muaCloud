
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
            // Its possible to multiselect several files including not available offline files.
            // If it is not available offline, we will ignore it.
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
