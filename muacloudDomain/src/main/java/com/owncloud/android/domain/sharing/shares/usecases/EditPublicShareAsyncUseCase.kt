

package com.owncloud.android.domain.sharing.shares.usecases

import com.owncloud.android.domain.sharing.shares.ShareRepository
import com.owncloud.android.domain.BaseUseCaseWithResult

class EditPublicShareAsyncUseCase(
    private val shareRepository: ShareRepository
) : BaseUseCaseWithResult<Unit, EditPublicShareAsyncUseCase.Params>() {

    override fun run(params: Params) {
        shareRepository.updatePublicShare(
            params.remoteId,
            params.name,
            params.password,
            params.expirationDateInMillis,
            params.permissions,
            params.accountName
        )
    }

    data class Params(
        val remoteId: String,
        val name: String,
        val password: String?,
        val expirationDateInMillis: Long,
        val permissions: Int,
        val accountName: String
    )
}
