

package com.owncloud.android.domain.files.usecases

import com.owncloud.android.domain.BaseUseCaseWithResult
import com.owncloud.android.domain.files.FileRepository
import com.owncloud.android.domain.files.model.OCFile

class IsAnyFileAvailableLocallyAndNotAvailableOfflineUseCase(private val fileRepository: FileRepository) :
    BaseUseCaseWithResult<Boolean, IsAnyFileAvailableLocallyAndNotAvailableOfflineUseCase.Params>() {

    override fun run(params: Params): Boolean = isAnyFileAvailableLocallyAndNotAvailableOffline(params.listOfFiles)
    private fun isAnyFileAvailableLocallyAndNotAvailableOffline(filesToRemove: List<OCFile>): Boolean {

        if (filesToRemove.any { it.isAvailableLocally && !it.isAvailableOffline }) {
            return true
        } else {
            filesToRemove.filter { it.isFolder }.forEach { folder ->
                if (isAnyFileAvailableLocallyAndNotAvailableOffline(fileRepository.getFolderContent(folder.id!!))) {
                    return true
                }
            }
        }
        return false
    }

    data class Params(val listOfFiles: List<OCFile>)

}
