
package com.owncloud.android.data.oauth.datasources

import com.owncloud.android.domain.authentication.oauth.model.ClientRegistrationInfo
import com.owncloud.android.domain.authentication.oauth.model.ClientRegistrationRequest
import com.owncloud.android.domain.authentication.oauth.model.OIDCServerConfiguration
import com.owncloud.android.domain.authentication.oauth.model.TokenRequest
import com.owncloud.android.domain.authentication.oauth.model.TokenResponse

interface RemoteOAuthDataSource {
    fun performOIDCDiscovery(baseUrl: String): OIDCServerConfiguration
    fun performTokenRequest(tokenRequest: TokenRequest): TokenResponse

    fun registerClient(clientRegistrationRequest: ClientRegistrationRequest): ClientRegistrationInfo

}
