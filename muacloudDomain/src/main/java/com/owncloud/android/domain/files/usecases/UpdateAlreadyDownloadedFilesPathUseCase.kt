

package com.owncloud.android.domain.files.usecases

import com.owncloud.android.domain.BaseUseCase
import com.owncloud.android.domain.files.FileRepository

class UpdateAlreadyDownloadedFilesPathUseCase(
    private val fileRepository: FileRepository,
) : BaseUseCase<Unit, UpdateAlreadyDownloadedFilesPathUseCase.Params>() {

    override fun run(params: Params) {
        fileRepository.updateDownloadedFilesStorageDirectoryInStoragePath(params.oldDirectory, params.newDirectory)
    }

    data class Params(
        val oldDirectory: String,
        val newDirectory: String
    )
}
