
package com.owncloud.android.domain.files.usecases

import com.owncloud.android.domain.BaseUseCase
import com.owncloud.android.domain.files.FileRepository
import com.owncloud.android.domain.files.model.OCFile


class GetPersonalRootFolderForAccountUseCase(
    private val fileRepository: FileRepository
) : BaseUseCase<OCFile?, GetPersonalRootFolderForAccountUseCase.Params>() {

    override fun run(params: Params): OCFile =
        fileRepository.getPersonalRootFolderForAccount(owner = params.owner)

    data class Params(
        val owner: String,
    )
}
