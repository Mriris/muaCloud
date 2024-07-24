

package com.owncloud.android.domain.files.usecases

import com.owncloud.android.domain.BaseUseCaseWithResult
import com.owncloud.android.domain.files.FileRepository
import com.owncloud.android.domain.files.model.OCFile

class SaveFileOrFolderUseCase(
    private val fileRepository: FileRepository
) : BaseUseCaseWithResult<Unit, SaveFileOrFolderUseCase.Params>() {
    override fun run(params: Params): Unit = fileRepository.saveFile(params.fileToSave)

    data class Params(val fileToSave: OCFile)
}
