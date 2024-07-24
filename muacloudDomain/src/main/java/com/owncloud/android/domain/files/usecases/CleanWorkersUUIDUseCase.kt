

package com.owncloud.android.domain.files.usecases

import com.owncloud.android.domain.BaseUseCaseWithResult
import com.owncloud.android.domain.files.FileRepository

class CleanWorkersUUIDUseCase(
    private val fileRepository: FileRepository
) : BaseUseCaseWithResult<Unit, CleanWorkersUUIDUseCase.Params>() {
    override fun run(params: Params) =
        fileRepository.cleanWorkersUuid(params.fileId)

    data class Params(
        val fileId: Long
    )
}
