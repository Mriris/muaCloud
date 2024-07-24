

package com.owncloud.android.domain.authentication.usecases

import com.owncloud.android.domain.BaseUseCaseWithResult
import com.owncloud.android.domain.authentication.AuthenticationRepository
import com.owncloud.android.domain.server.model.ServerInfo

class LoginBasicAsyncUseCase(
    private val authenticationRepository: AuthenticationRepository
) : BaseUseCaseWithResult<String, LoginBasicAsyncUseCase.Params>() {

    override fun run(params: Params): String {
        require(params.serverInfo != null) { "Invalid server info" }
        require(params.username.isNotEmpty()) { "Invalid username" }
        require(params.password.isNotEmpty()) { "Invalid password" }

        return authenticationRepository.loginBasic(
            params.serverInfo,
            params.username,
            params.password,
            params.updateAccountWithUsername
        )
    }

    data class Params(
        val serverInfo: ServerInfo?,
        val username: String,
        val password: String,
        val updateAccountWithUsername: String? = null
    )
}
