
package com.owncloud.android.testutil.oauth

import com.owncloud.android.domain.authentication.oauth.model.OIDCServerConfiguration

val OC_OIDC_SERVER_CONFIGURATION = OIDCServerConfiguration(
    authorizationEndpoint = "https://owncloud.server/authorize",
    checkSessionIframe = "https://owncloud.server/check-session.html",
    endSessionEndpoint = "https://owncloud.server/endsession",
    issuer = "https://owncloud.server/",
    registrationEndpoint = "https://owncloud.server/register",
    responseTypesSupported = listOf(
        "id_token token",
        "id_token",
        "code id_token",
        "code id_token token"
    ),
    scopesSupported = listOf(
        "openid",
        "offline_access",
        "konnect/raw_sub",
        "profile",
        "email",
        "konnect/uuid"
    ),
    tokenEndpoint = "https://owncloud.server/token",
    tokenEndpointAuthMethodsSupported = listOf(),
    userInfoEndpoint = "https://owncloud.server/userinfo"
)
