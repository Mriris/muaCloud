

package com.owncloud.android.domain.sharing.shares.usecases

import com.owncloud.android.domain.BaseUseCaseWithResult
import com.owncloud.android.domain.sharing.shares.ShareRepository

class DeleteShareAsyncUseCase(
    private val shareRepository: ShareRepository
) : BaseUseCaseWithResult<Unit, DeleteShareAsyncUseCase.Params>() {
    override fun run(params: Params): Unit =
        shareRepository.deleteShare(
            remoteId = params.remoteId,
            accountName = params.accountName,
        )

    data class Params(val remoteId: String, val accountName: String)
}
