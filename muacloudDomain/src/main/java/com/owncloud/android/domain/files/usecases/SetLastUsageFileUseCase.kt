

package com.owncloud.android.domain.files.usecases

import com.owncloud.android.domain.BaseUseCaseWithResult
import com.owncloud.android.domain.files.FileRepository

class SetLastUsageFileUseCase(
    private val fileRepository: FileRepository,
) : BaseUseCaseWithResult<Unit, SetLastUsageFileUseCase.Params>() {

    override fun run(params: Params) {
        if (params.isAvailableLocally && !params.isFolder) {
            fileRepository.updateFileWithLastUsage(params.fileId, params.lastUsage)
        }
    }

    data class Params(
        val fileId: Long,
        val lastUsage: Long?,
        val isAvailableLocally: Boolean,
        val isFolder: Boolean,
    )
}
