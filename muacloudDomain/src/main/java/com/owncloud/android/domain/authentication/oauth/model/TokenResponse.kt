

package com.owncloud.android.domain.authentication.oauth.model

data class TokenResponse(
    val accessToken: String,
    val expiresIn: Int,
    val refreshToken: String?,
    val tokenType: String,
    val userId: String?,
    val scope: String?,
    val additionalParameters: Map<String, String>?
)
