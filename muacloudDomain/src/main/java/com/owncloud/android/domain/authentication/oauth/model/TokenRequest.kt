
package com.owncloud.android.domain.authentication.oauth.model

sealed class TokenRequest(
    val baseUrl: String,
    val tokenEndpoint: String,
    val clientAuth: String,
    val grantType: String,
    val scope: String,
) {
    class AccessToken(
        baseUrl: String,
        tokenEndpoint: String,
        clientAuth: String,
        scope: String,
        val authorizationCode: String,
        val redirectUri: String,
        val codeVerifier: String
    ) : TokenRequest(baseUrl, tokenEndpoint, clientAuth, GrantType.ACCESS_TOKEN.string, scope)

    class RefreshToken(
        baseUrl: String,
        tokenEndpoint: String,
        clientAuth: String,
        scope: String,
        val refreshToken: String? = null
    ) : TokenRequest(baseUrl, tokenEndpoint, clientAuth, GrantType.REFRESH_TOKEN.string, scope)

    enum class GrantType(val string: String) {
        /** Request access token. [More info](https://tools.ietf.org/html/rfc6749#section-4.1.3) */
        ACCESS_TOKEN("authorization_code"),

        /** Refresh access token. [More info](https://tools.ietf.org/html/rfc6749#section-6) */
        REFRESH_TOKEN("refresh_token")
    }
}
