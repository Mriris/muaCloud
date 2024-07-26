package com.owncloud.android.domain.availableoffline.usecases

import com.owncloud.android.domain.BaseUseCaseWithResult
import com.owncloud.android.domain.files.FileRepository
import com.owncloud.android.domain.files.model.OCFile

class GetFilesAvailableOfflineFromAccountUseCase(
    private val fileRepository: FileRepository
) : BaseUseCaseWithResult<List<OCFile>, GetFilesAvailableOfflineFromAccountUseCase.Params>() {

    override fun run(params: Params): List<OCFile> = fileRepository.getFilesAvailableOfflineFromAccount(params.owner)

    data class Params(
        val owner: String
    )
}
