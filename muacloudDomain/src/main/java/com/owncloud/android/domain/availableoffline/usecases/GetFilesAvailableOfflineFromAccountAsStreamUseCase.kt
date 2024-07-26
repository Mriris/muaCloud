package com.owncloud.android.domain.availableoffline.usecases

import com.owncloud.android.domain.BaseUseCase
import com.owncloud.android.domain.files.FileRepository
import com.owncloud.android.domain.files.model.OCFileWithSyncInfo
import kotlinx.coroutines.flow.Flow

class GetFilesAvailableOfflineFromAccountAsStreamUseCase(
    private val fileRepository: FileRepository
) : BaseUseCase<Flow<List<OCFileWithSyncInfo>>, GetFilesAvailableOfflineFromAccountAsStreamUseCase.Params>() {

    override fun run(params: Params): Flow<List<OCFileWithSyncInfo>> = fileRepository.getFilesWithSyncInfoAvailableOfflineFromAccountAsFlow(params.owner)

    data class Params(
        val owner: String
    )
}
