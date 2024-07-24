

package com.owncloud.android.domain.files.usecases

import com.owncloud.android.domain.BaseUseCaseWithResult
import com.owncloud.android.domain.files.FileRepository

class CleanConflictUseCase(
    private val fileRepository: FileRepository
) : BaseUseCaseWithResult<Unit, CleanConflictUseCase.Params>() {
    override fun run(params: Params) =
        fileRepository.cleanConflict(params.fileId)

    data class Params(
        val fileId: Long
    )
}
