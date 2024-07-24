

package com.owncloud.android.usecases.files

import com.owncloud.android.domain.BaseUseCaseWithResult
import com.owncloud.android.domain.files.FileRepository
import com.owncloud.android.domain.files.usecases.RemoveFileUseCase

class RemoveLocalFilesForAccountUseCase(
    private val fileRepository: FileRepository,
    private val removeFileUseCase: RemoveFileUseCase,
) : BaseUseCaseWithResult<Unit, RemoveLocalFilesForAccountUseCase.Params>() {

    override fun run(params: Params) {
        val listOfFilesToDelete = fileRepository.getDownloadedFilesForAccount(params.owner)
        if (listOfFilesToDelete.isNotEmpty()) {
            removeFileUseCase(RemoveFileUseCase.Params(listOfFilesToDelete, true))
        }
    }

    data class Params(
        val owner: String,
    )
}
