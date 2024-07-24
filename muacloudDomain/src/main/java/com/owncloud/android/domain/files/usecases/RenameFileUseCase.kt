
package com.owncloud.android.domain.files.usecases

import com.owncloud.android.domain.BaseUseCaseWithResult
import com.owncloud.android.domain.files.FileRepository
import com.owncloud.android.domain.files.model.OCFile
import com.owncloud.android.domain.validator.FileNameValidator

class RenameFileUseCase(
    private val fileRepository: FileRepository,
    private val setLastUsageFileUseCase: SetLastUsageFileUseCase,
) : BaseUseCaseWithResult<Unit, RenameFileUseCase.Params>() {

    private val fileNameValidator = FileNameValidator()

    override fun run(params: Params) {
        fileNameValidator.validateOrThrowException(params.newName)
        val isAvailableLocally = params.ocFileToRename.isAvailableLocally

        fileRepository.renameFile(
            ocFile = params.ocFileToRename,
            newName = params.newName,
        )
        setLastUsageFile(params.ocFileToRename, isAvailableLocally)
    }

    private fun setLastUsageFile(file: OCFile, isAvailableLocally: Boolean) {
        setLastUsageFileUseCase(
            SetLastUsageFileUseCase.Params(
                fileId = file.id!!,
                lastUsage = System.currentTimeMillis(),
                isAvailableLocally = isAvailableLocally,
                isFolder = file.isFolder,
            )
        )
    }

    data class Params(
        val ocFileToRename: OCFile,
        val newName: String
    )
}
