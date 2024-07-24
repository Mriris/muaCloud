
package com.owncloud.android.domain.files.usecases

import com.owncloud.android.domain.BaseUseCase
import com.owncloud.android.domain.files.FileRepository
import com.owncloud.android.domain.files.model.OCFile
import kotlinx.coroutines.flow.Flow

class GetFileByIdAsStreamUseCase(
    private val fileRepository: FileRepository
) : BaseUseCase<Flow<OCFile?>, GetFileByIdAsStreamUseCase.Params>() {

    override fun run(params: Params): Flow<OCFile?> =
        fileRepository.getFileByIdAsFlow(params.fileId)

    data class Params(val fileId: Long)

}
