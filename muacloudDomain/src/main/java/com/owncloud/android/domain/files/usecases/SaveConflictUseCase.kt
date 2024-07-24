

package com.owncloud.android.domain.files.usecases

import com.owncloud.android.domain.BaseUseCaseWithResult
import com.owncloud.android.domain.files.FileRepository

class SaveConflictUseCase(
    private val fileRepository: FileRepository
) : BaseUseCaseWithResult<Unit, SaveConflictUseCase.Params>() {
    override fun run(params: Params) =
        fileRepository.saveConflict(params.fileId, params.eTagInConflict)

    data class Params(
        val fileId: Long,
        val eTagInConflict: String
    )
}
