package com.owncloud.android.lib.resources.oauth.services.implementation

import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.resources.oauth.GetOIDCDiscoveryRemoteOperation
import com.owncloud.android.lib.resources.oauth.RegisterClientRemoteOperation
import com.owncloud.android.lib.resources.oauth.TokenRequestRemoteOperation
import com.owncloud.android.lib.resources.oauth.params.ClientRegistrationParams
import com.owncloud.android.lib.resources.oauth.params.TokenRequestParams
import com.owncloud.android.lib.resources.oauth.responses.ClientRegistrationResponse
import com.owncloud.android.lib.resources.oauth.responses.OIDCDiscoveryResponse
import com.owncloud.android.lib.resources.oauth.responses.TokenResponse
import com.owncloud.android.lib.resources.oauth.services.OIDCService

class OCOIDCService : OIDCService {

    override fun getOIDCServerDiscovery(
        ownCloudClient: OwnCloudClient
    ): RemoteOperationResult<OIDCDiscoveryResponse> =
        GetOIDCDiscoveryRemoteOperation().execute(ownCloudClient)

    override fun performTokenRequest(
        ownCloudClient: OwnCloudClient,
        tokenRequest: TokenRequestParams
    ): RemoteOperationResult<TokenResponse> =
        TokenRequestRemoteOperation(tokenRequest).execute(ownCloudClient)

    override fun registerClientWithRegistrationEndpoint(
        ownCloudClient: OwnCloudClient,
        clientRegistrationParams: ClientRegistrationParams
    ): RemoteOperationResult<ClientRegistrationResponse> =
        RegisterClientRemoteOperation(clientRegistrationParams).execute(ownCloudClient)

}
