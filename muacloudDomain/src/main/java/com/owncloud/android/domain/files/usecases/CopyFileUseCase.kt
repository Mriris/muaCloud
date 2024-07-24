

package com.owncloud.android.domain.files.usecases

import com.owncloud.android.domain.BaseUseCaseWithResult
import com.owncloud.android.domain.exceptions.CopyIntoDescendantException
import com.owncloud.android.domain.files.FileRepository
import com.owncloud.android.domain.files.model.OCFile


class CopyFileUseCase(
    private val fileRepository: FileRepository,
) : BaseUseCaseWithResult<List<OCFile>, CopyFileUseCase.Params>() {

    override fun run(params: Params): List<OCFile> {
        validateOrThrowException(params.listOfFilesToCopy, params.targetFolder)
        return fileRepository.copyFile(
            listOfFilesToCopy = params.listOfFilesToCopy,
            targetFolder = params.targetFolder,
            replace = params.replace,
            isUserLogged = params.isUserLogged,
        )
    }

    @Throws(IllegalArgumentException::class, CopyIntoDescendantException::class)
    fun validateOrThrowException(listOfFilesToCopy: List<OCFile>, targetFolder: OCFile) {
        require(listOfFilesToCopy.isNotEmpty())

        if (listOfFilesToCopy.any { targetFolder.remotePath.startsWith(it.remotePath) && targetFolder.spaceId == it.spaceId }) {
            throw CopyIntoDescendantException()
        }
    }

    data class Params(
        val listOfFilesToCopy: List<OCFile>,
        val targetFolder: OCFile,
        val replace: List<Boolean?> = emptyList(),
        val isUserLogged: Boolean,
    )
}
