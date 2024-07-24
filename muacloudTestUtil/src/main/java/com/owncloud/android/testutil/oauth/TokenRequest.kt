
package com.owncloud.android.testutil.oauth

import com.owncloud.android.domain.authentication.oauth.model.TokenRequest
import com.owncloud.android.testutil.OC_SECURE_BASE_URL
import com.owncloud.android.testutil.OC_CLIENT_AUTH
import com.owncloud.android.testutil.OC_REDIRECT_URI
import com.owncloud.android.testutil.OC_REFRESH_TOKEN
import com.owncloud.android.testutil.OC_SCOPE
import com.owncloud.android.testutil.OC_TOKEN_ENDPOINT

val OC_TOKEN_REQUEST_REFRESH = TokenRequest.RefreshToken(
    baseUrl = OC_SECURE_BASE_URL,
    tokenEndpoint = OC_TOKEN_ENDPOINT,
    clientAuth = OC_CLIENT_AUTH,
    scope = OC_SCOPE,
    refreshToken = OC_REFRESH_TOKEN
)

val OC_TOKEN_REQUEST_ACCESS = TokenRequest.AccessToken(
    baseUrl = OC_SECURE_BASE_URL,
    tokenEndpoint = OC_TOKEN_ENDPOINT,
    clientAuth = OC_CLIENT_AUTH,
    scope = OC_SCOPE,
    authorizationCode = "4uth0r1z4t10nC0d3",
    redirectUri = OC_REDIRECT_URI,
    codeVerifier = "A high-entropy cryptographic random STRING using the unreserved characters"
)
