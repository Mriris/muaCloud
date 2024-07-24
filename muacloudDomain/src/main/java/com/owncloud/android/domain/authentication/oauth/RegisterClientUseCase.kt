
package com.owncloud.android.domain.authentication.oauth

import com.owncloud.android.domain.BaseUseCaseWithResult
import com.owncloud.android.domain.authentication.oauth.model.ClientRegistrationInfo
import com.owncloud.android.domain.authentication.oauth.model.ClientRegistrationRequest

class RegisterClientUseCase(
    private val oAuthRepository: OAuthRepository
) : BaseUseCaseWithResult<ClientRegistrationInfo, RegisterClientUseCase.Params>() {

    override fun run(params: Params): ClientRegistrationInfo =
        oAuthRepository.registerClient(clientRegistrationRequest = params.clientRegistrationRequest)

    data class Params(
        val clientRegistrationRequest: ClientRegistrationRequest
    )
}
