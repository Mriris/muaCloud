
package com.owncloud.android.domain.files.usecases

import com.owncloud.android.domain.BaseUseCase
import com.owncloud.android.domain.files.FileRepository
import com.owncloud.android.domain.files.model.OCFileWithSyncInfo
import kotlinx.coroutines.flow.Flow

class GetFolderContentAsStreamUseCase(
    private val repository: FileRepository
) : BaseUseCase<Flow<List<OCFileWithSyncInfo>>, GetFolderContentAsStreamUseCase.Params>() {

    override fun run(params: Params) = repository.getFolderContentWithSyncInfoAsFlow(params.folderId)

    data class Params(val folderId: Long)

}
