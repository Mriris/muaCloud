

package com.owncloud.android.domain.authentication.usecases

import com.owncloud.android.domain.BaseUseCaseWithResult
import com.owncloud.android.domain.authentication.AuthenticationRepository
import com.owncloud.android.domain.authentication.oauth.model.ClientRegistrationInfo
import com.owncloud.android.domain.server.model.ServerInfo

class LoginOAuthAsyncUseCase(
    private val authenticationRepository: AuthenticationRepository
) : BaseUseCaseWithResult<String, LoginOAuthAsyncUseCase.Params>() {

    override fun run(params: Params): String {
        require(params.serverInfo != null) { "Invalid server info" }
        require(params.authTokenType.isNotEmpty()) { "Invalid authorization token type" }
        require(params.accessToken.isNotEmpty()) { "Invalid access token" }
        require(params.refreshToken.isNotEmpty()) { "Invalid refresh token" }

        val accountName = authenticationRepository.loginOAuth(
            params.serverInfo,
            params.username,
            params.authTokenType,
            params.accessToken,
            params.refreshToken,
            params.scope,
            params.updateAccountWithUsername,
            params.clientRegistrationInfo
        )

        return accountName
    }

    data class Params(
        val serverInfo: ServerInfo?,
        val username: String,
        val authTokenType: String,
        val accessToken: String,
        val refreshToken: String,
        val scope: String?,
        val updateAccountWithUsername: String?,
        val clientRegistrationInfo: ClientRegistrationInfo?
    )
}
