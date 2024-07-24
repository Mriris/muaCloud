

package com.owncloud.android.domain.files.usecases

import com.owncloud.android.domain.BaseUseCaseWithResult
import com.owncloud.android.domain.files.FileRepository
import java.util.UUID

class SaveDownloadWorkerUUIDUseCase(
    private val fileRepository: FileRepository
) : BaseUseCaseWithResult<Unit, SaveDownloadWorkerUUIDUseCase.Params>() {
    override fun run(params: Params) =
        fileRepository.saveDownloadWorkerUuid(params.fileId, params.workerUuid)

    data class Params(
        val fileId: Long,
        val workerUuid: UUID
    )
}
