
package com.owncloud.android.testutil.oauth

import com.owncloud.android.domain.authentication.oauth.model.ClientRegistrationRequest
import com.owncloud.android.testutil.OC_REDIRECT_URI

val OC_CLIENT_REGISTRATION_REQUEST = ClientRegistrationRequest(
    registrationEndpoint = OC_OIDC_SERVER_CONFIGURATION.registrationEndpoint!!,
    clientName = "Android Client v2.17",
    redirectUris = listOf(OC_REDIRECT_URI),
    tokenEndpointAuthMethod = "client_secret_basic",
    applicationType = "native"
)
