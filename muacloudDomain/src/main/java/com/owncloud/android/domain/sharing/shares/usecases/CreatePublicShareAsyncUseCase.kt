

package com.owncloud.android.domain.sharing.shares.usecases

import com.owncloud.android.domain.BaseUseCaseWithResult
import com.owncloud.android.domain.sharing.shares.ShareRepository

class CreatePublicShareAsyncUseCase(
    private val shareRepository: ShareRepository
) : BaseUseCaseWithResult<Unit, CreatePublicShareAsyncUseCase.Params>() {

    override fun run(params: Params) =
        shareRepository.insertPublicShare(
            params.filePath,
            params.permissions,
            params.name,
            params.password,
            params.expirationTimeInMillis,
            params.accountName
        )

    data class Params(
        val filePath: String,
        val permissions: Int,
        val name: String,
        val password: String,
        val expirationTimeInMillis: Long,
        val accountName: String
    )
}
