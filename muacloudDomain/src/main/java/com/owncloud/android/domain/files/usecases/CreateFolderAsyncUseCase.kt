
package com.owncloud.android.domain.files.usecases

import com.owncloud.android.domain.BaseUseCaseWithResult
import com.owncloud.android.domain.files.FileRepository
import com.owncloud.android.domain.files.model.OCFile
import com.owncloud.android.domain.validator.FileNameValidator

class CreateFolderAsyncUseCase(
    private val fileRepository: FileRepository
) : BaseUseCaseWithResult<Unit, CreateFolderAsyncUseCase.Params>() {

    private val fileNameValidator = FileNameValidator()

    override fun run(params: Params) {
        fileNameValidator.validateOrThrowException(params.folderName)

        val remotePath = params.parentFile.remotePath.plus(params.folderName).plus(OCFile.PATH_SEPARATOR)
        return fileRepository.createFolder(remotePath = remotePath, parentFolder = params.parentFile)
    }

    data class Params(val folderName: String, val parentFile: OCFile)
}
