

package com.owncloud.android.domain.sharing.shares.usecases

import com.owncloud.android.domain.sharing.shares.ShareRepository
import com.owncloud.android.domain.BaseUseCaseWithResult

class EditPrivateShareAsyncUseCase(
    private val shareRepository: ShareRepository
) : BaseUseCaseWithResult<Unit, EditPrivateShareAsyncUseCase.Params>() {

    override fun run(params: Params): Unit =
        shareRepository.updatePrivateShare(
            params.remoteId,
            params.permissions,
            params.accountName
        )

    data class Params(
        val remoteId: String,
        val permissions: Int,
        val accountName: String
    )
}
