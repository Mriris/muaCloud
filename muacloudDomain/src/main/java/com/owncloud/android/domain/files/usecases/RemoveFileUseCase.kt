
package com.owncloud.android.domain.files.usecases

import com.owncloud.android.domain.BaseUseCaseWithResult
import com.owncloud.android.domain.files.FileRepository
import com.owncloud.android.domain.files.model.OCFile

class RemoveFileUseCase(
    private val fileRepository: FileRepository,
) : BaseUseCaseWithResult<Unit, RemoveFileUseCase.Params>() {

    override fun run(params: Params) {

        require(params.listOfFilesToDelete.isNotEmpty())

        return fileRepository.deleteFiles(
            listOfFilesToDelete = params.listOfFilesToDelete,
            removeOnlyLocalCopy = params.removeOnlyLocalCopy,
        )

    }

    data class Params(
        val listOfFilesToDelete: List<OCFile>,
        val removeOnlyLocalCopy: Boolean
    )
}
