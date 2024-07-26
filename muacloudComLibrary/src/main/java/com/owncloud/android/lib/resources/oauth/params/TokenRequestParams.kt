package com.owncloud.android.lib.resources.oauth.params

import com.owncloud.android.lib.common.http.HttpConstants
import okhttp3.FormBody
import okhttp3.RequestBody

sealed class TokenRequestParams(
    val tokenEndpoint: String,
    val clientAuth: String,
    val grantType: String,
    val scope: String,
) {
    abstract fun toRequestBody(): RequestBody

    class Authorization(
        tokenEndpoint: String,
        clientAuth: String,
        grantType: String,
        scope: String,
        val authorizationCode: String,
        val redirectUri: String,
        val codeVerifier: String,
    ) : TokenRequestParams(tokenEndpoint, clientAuth, grantType, scope) {

        override fun toRequestBody(): RequestBody =
            FormBody.Builder()
                .add(HttpConstants.OAUTH_HEADER_AUTHORIZATION_CODE, authorizationCode)
                .add(HttpConstants.OAUTH_HEADER_GRANT_TYPE, grantType)
                .add(HttpConstants.OAUTH_HEADER_REDIRECT_URI, redirectUri)
                .add(HttpConstants.OAUTH_HEADER_CODE_VERIFIER, codeVerifier)
                .add(HttpConstants.OAUTH_HEADER_SCOPE, scope)
                .build()
    }

    class RefreshToken(
        tokenEndpoint: String,
        clientAuth: String,
        grantType: String,
        scope: String,
        val refreshToken: String? = null
    ) : TokenRequestParams(tokenEndpoint, clientAuth, grantType, scope) {

        override fun toRequestBody(): RequestBody =
            FormBody.Builder().apply {
                add(HttpConstants.OAUTH_HEADER_GRANT_TYPE, grantType)
                add(HttpConstants.OAUTH_HEADER_SCOPE, scope)
                if (!refreshToken.isNullOrBlank()) {
                    add(HttpConstants.OAUTH_HEADER_REFRESH_TOKEN, refreshToken)
                }
            }.build()

    }
}
