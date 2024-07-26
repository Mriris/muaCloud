package com.owncloud.android.domain.files.usecases

import com.owncloud.android.domain.BaseUseCase
import com.owncloud.android.domain.files.FileRepository
import com.owncloud.android.domain.files.model.OCFileWithSyncInfo
import kotlinx.coroutines.flow.Flow

class GetSharedByLinkForAccountAsStreamUseCase(
    private val fileRepository: FileRepository
) : BaseUseCase<Flow<List<OCFileWithSyncInfo>>, GetSharedByLinkForAccountAsStreamUseCase.Params>() {

    override fun run(params: Params): Flow<List<OCFileWithSyncInfo>> = fileRepository.getSharedByLinkWithSyncInfoForAccountAsFlow(params.owner)

    data class Params(
        val owner: String
    )
}
