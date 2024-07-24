
package com.owncloud.android.domain.availableoffline.usecases

import com.owncloud.android.domain.BaseUseCaseWithResult
import com.owncloud.android.domain.availableoffline.model.AvailableOfflineStatus
import com.owncloud.android.domain.files.FileRepository
import com.owncloud.android.domain.files.model.OCFile

class SetFilesAsAvailableOfflineUseCase(
    private val fileRepository: FileRepository,
) : BaseUseCaseWithResult<Unit, SetFilesAsAvailableOfflineUseCase.Params>() {

    override fun run(params: Params) {
        params.filesToSetAsAvailableOffline.forEach { fileToSetAsAvailableOffline ->
            // Its possible to multiselect several files including already available offline files.
            // If it is already available offline, we will ignore it.
            if (!fileToSetAsAvailableOffline.isAvailableOffline) {
                fileRepository.updateFileWithNewAvailableOfflineStatus(
                    ocFile = fileToSetAsAvailableOffline,
                    newAvailableOfflineStatus = AvailableOfflineStatus.AVAILABLE_OFFLINE,
                )
            }
        }
    }

    data class Params(
        val filesToSetAsAvailableOffline: List<OCFile>
    )
}
