
package com.owncloud.android.domain.files.usecases

import com.owncloud.android.domain.BaseUseCase
import com.owncloud.android.domain.files.FileRepository
import com.owncloud.android.domain.files.model.OCFile


class GetSharesRootFolderForAccount(
    private val fileRepository: FileRepository
) : BaseUseCase<OCFile?, GetSharesRootFolderForAccount.Params>() {

    override fun run(params: Params): OCFile? =
        fileRepository.getSharesRootFolderForAccount(owner = params.owner)

    data class Params(
        val owner: String,
    )
}
