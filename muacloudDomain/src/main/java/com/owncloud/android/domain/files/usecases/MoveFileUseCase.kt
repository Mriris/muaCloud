

package com.owncloud.android.domain.files.usecases

import com.owncloud.android.domain.BaseUseCaseWithResult
import com.owncloud.android.domain.exceptions.MoveIntoDescendantException
import com.owncloud.android.domain.exceptions.MoveIntoSameFolderException
import com.owncloud.android.domain.exceptions.MoveIntoAnotherSpaceException
import com.owncloud.android.domain.files.FileRepository
import com.owncloud.android.domain.files.model.OCFile


class MoveFileUseCase(
    private val fileRepository: FileRepository,
    private val setLastUsageFileUseCase: SetLastUsageFileUseCase,
) : BaseUseCaseWithResult<List<OCFile>, MoveFileUseCase.Params>() {

    override fun run(params: Params): List<OCFile> {
        validateOrThrowException(params.listOfFilesToMove, params.targetFolder)

        val listOfFilesToMoveOriginal = params.listOfFilesToMove.map { it to it.isAvailableLocally }

        val listOfFilesToMove = fileRepository.moveFile(
            listOfFilesToMove = params.listOfFilesToMove,
            targetFolder = params.targetFolder,
            replace = params.replace,
            isUserLogged = params.isUserLogged,
        )

        listOfFilesToMoveOriginal.forEach { (ocFile, isAvailableLocally) ->
            setLastUsageFile(ocFile, isAvailableLocally)
        }

        return listOfFilesToMove
    }

    @Throws(
        IllegalArgumentException::class,
        MoveIntoSameFolderException::class,
        MoveIntoDescendantException::class,
        MoveIntoAnotherSpaceException::class
    )
    fun validateOrThrowException(listOfFilesToMove: List<OCFile>, targetFolder: OCFile) {
        require(listOfFilesToMove.isNotEmpty())
        if (listOfFilesToMove[0].spaceId != targetFolder.spaceId) {
            throw MoveIntoAnotherSpaceException()
        } else if (listOfFilesToMove.any { targetFolder.remotePath.startsWith(it.remotePath) }) {
            throw MoveIntoDescendantException()
        } else if (listOfFilesToMove.any { it.parentId == targetFolder.id }) {
            throw MoveIntoSameFolderException()
        }
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
        val listOfFilesToMove: List<OCFile>,
        val targetFolder: OCFile,
        val replace: List<Boolean?> = emptyList(),
        val isUserLogged: Boolean,
    )
}
