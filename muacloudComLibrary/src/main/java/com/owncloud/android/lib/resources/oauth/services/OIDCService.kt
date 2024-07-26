package com.owncloud.android.lib.resources.oauth.services

import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.resources.oauth.params.ClientRegistrationParams
import com.owncloud.android.lib.resources.oauth.params.TokenRequestParams
import com.owncloud.android.lib.resources.oauth.responses.ClientRegistrationResponse
import com.owncloud.android.lib.resources.oauth.responses.OIDCDiscoveryResponse
import com.owncloud.android.lib.resources.oauth.responses.TokenResponse

interface OIDCService {

    fun getOIDCServerDiscovery(ownCloudClient: OwnCloudClient): RemoteOperationResult<OIDCDiscoveryResponse>

    fun performTokenRequest(
        ownCloudClient: OwnCloudClient,
        tokenRequest: TokenRequestParams
    ): RemoteOperationResult<TokenResponse>

    fun registerClientWithRegistrationEndpoint(
        ownCloudClient: OwnCloudClient,
        clientRegistrationParams: ClientRegistrationParams
    ): RemoteOperationResult<ClientRegistrationResponse>
}
