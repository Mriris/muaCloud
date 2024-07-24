

package com.owncloud.android.domain.sharing.shares.usecases

import com.owncloud.android.domain.sharing.shares.ShareRepository
import com.owncloud.android.domain.BaseUseCaseWithResult

class RefreshSharesFromServerAsyncUseCase(
    private val shareRepository: ShareRepository
) : BaseUseCaseWithResult<Unit, RefreshSharesFromServerAsyncUseCase.Params>() {

    override fun run(params: Params) =
        shareRepository.refreshSharesFromNetwork(
            params.filePath,
            params.accountName
        )

    data class Params(
        val filePath: String,
        val accountName: String
    )
}
