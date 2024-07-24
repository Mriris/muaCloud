
package com.owncloud.android.domain.files.usecases

import com.owncloud.android.domain.BaseUseCaseWithResult
import com.owncloud.android.domain.files.FileRepository
import com.owncloud.android.domain.files.model.OCFile

class GetFileByRemotePathUseCase(
    private val fileRepository: FileRepository
) : BaseUseCaseWithResult<OCFile?, GetFileByRemotePathUseCase.Params>() {

    override fun run(params: Params): OCFile? =
        fileRepository.getFileByRemotePath(
            params.remotePath,
            params.owner,
            params.spaceId,
        )

    data class Params(
        val owner: String,
        val remotePath: String,
        val spaceId: String? = null,
    )

}
