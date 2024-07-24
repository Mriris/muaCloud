
package com.owncloud.android.domain.authentication.oauth

import com.owncloud.android.domain.BaseUseCaseWithResult
import com.owncloud.android.domain.authentication.oauth.model.TokenRequest
import com.owncloud.android.domain.authentication.oauth.model.TokenResponse

class RequestTokenUseCase(
    private val oAuthRepository: OAuthRepository
) : BaseUseCaseWithResult<TokenResponse, RequestTokenUseCase.Params>() {

    override fun run(params: Params): TokenResponse {
        return oAuthRepository.performTokenRequest(params.tokenRequest)
    }

    data class Params(
        val tokenRequest: TokenRequest
    )
}
