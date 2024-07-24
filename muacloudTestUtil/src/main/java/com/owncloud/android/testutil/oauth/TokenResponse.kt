
package com.owncloud.android.testutil.oauth

import com.owncloud.android.domain.authentication.oauth.model.TokenResponse
import com.owncloud.android.testutil.OC_ACCESS_TOKEN
import com.owncloud.android.testutil.OC_REFRESH_TOKEN

val OC_TOKEN_RESPONSE = TokenResponse(
    accessToken = OC_ACCESS_TOKEN,
    expiresIn = 3600,
    refreshToken = OC_REFRESH_TOKEN,
    tokenType = "Bearer",
    userId = "demo",
    scope = null,
    additionalParameters = mapOf()
)
