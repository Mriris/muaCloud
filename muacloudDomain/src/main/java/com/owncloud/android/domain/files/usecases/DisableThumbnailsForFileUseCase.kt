
package com.owncloud.android.domain.files.usecases

import com.owncloud.android.domain.BaseUseCaseWithResult
import com.owncloud.android.domain.files.FileRepository

class DisableThumbnailsForFileUseCase(
    private val fileRepository: FileRepository
) : BaseUseCaseWithResult<Unit, DisableThumbnailsForFileUseCase.Params>() {

    override fun run(params: Params): Unit =
        fileRepository.disableThumbnailsForFile(params.fileId)

    data class Params(val fileId: Long)

}
