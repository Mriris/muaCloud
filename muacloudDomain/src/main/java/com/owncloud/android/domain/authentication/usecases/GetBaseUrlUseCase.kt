

package com.owncloud.android.domain.authentication.usecases

import com.owncloud.android.domain.BaseUseCaseWithResult
import com.owncloud.android.domain.authentication.AuthenticationRepository

class GetBaseUrlUseCase(
    private val authenticationRepository: AuthenticationRepository
) : BaseUseCaseWithResult<String, GetBaseUrlUseCase.Params>() {

    override fun run(params: Params): String {
        require(params.accountName.isNotEmpty()) { "Invalid account name" }
        return authenticationRepository.getBaseUrl(params.accountName)
    }

    data class Params(
        val accountName: String
    )
}
