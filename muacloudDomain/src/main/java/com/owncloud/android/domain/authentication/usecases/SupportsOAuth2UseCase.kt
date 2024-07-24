

package com.owncloud.android.domain.authentication.usecases

import com.owncloud.android.domain.BaseUseCaseWithResult
import com.owncloud.android.domain.authentication.AuthenticationRepository

class SupportsOAuth2UseCase(
    private val authenticationRepository: AuthenticationRepository
) : BaseUseCaseWithResult<Boolean, SupportsOAuth2UseCase.Params>() {

    override fun run(params: Params): Boolean {
        require(params.accountName.isNotEmpty()) { "Invalid account name" }
        return authenticationRepository.supportsOAuth2UseCase(params.accountName)
    }

    data class Params(
        val accountName: String
    )
}
