
package com.owncloud.android.data.oauth.repository

import com.owncloud.android.data.oauth.datasources.RemoteOAuthDataSource
import com.owncloud.android.domain.authentication.oauth.OAuthRepository
import com.owncloud.android.domain.authentication.oauth.model.ClientRegistrationInfo
import com.owncloud.android.domain.authentication.oauth.model.ClientRegistrationRequest
import com.owncloud.android.domain.authentication.oauth.model.OIDCServerConfiguration
import com.owncloud.android.domain.authentication.oauth.model.TokenRequest
import com.owncloud.android.domain.authentication.oauth.model.TokenResponse

class OCOAuthRepository(
    private val oidcRemoteOAuthDataSource: RemoteOAuthDataSource,
) : OAuthRepository {

    override fun performOIDCDiscovery(baseUrl: String): OIDCServerConfiguration {
        return oidcRemoteOAuthDataSource.performOIDCDiscovery(baseUrl)
    }

    override fun performTokenRequest(tokenRequest: TokenRequest): TokenResponse =
        oidcRemoteOAuthDataSource.performTokenRequest(tokenRequest)

    override fun registerClient(clientRegistrationRequest: ClientRegistrationRequest): ClientRegistrationInfo =
        oidcRemoteOAuthDataSource.registerClient(clientRegistrationRequest)
}
