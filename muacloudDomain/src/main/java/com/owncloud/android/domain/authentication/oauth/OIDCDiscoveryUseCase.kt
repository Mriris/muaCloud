
package com.owncloud.android.domain.authentication.oauth

import com.owncloud.android.domain.BaseUseCaseWithResult
import com.owncloud.android.domain.authentication.oauth.model.OIDCServerConfiguration

class OIDCDiscoveryUseCase(
    private val oAuthRepository: OAuthRepository
) : BaseUseCaseWithResult<OIDCServerConfiguration, OIDCDiscoveryUseCase.Params>() {

    override fun run(params: Params): OIDCServerConfiguration {
        require(params.baseUrl.isNotEmpty()) { "Invalid URL" }
        return oAuthRepository.performOIDCDiscovery(params.baseUrl)
    }

    data class Params(
        val baseUrl: String
    )
}
