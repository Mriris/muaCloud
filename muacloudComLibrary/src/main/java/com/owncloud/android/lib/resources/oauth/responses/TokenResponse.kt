
package com.owncloud.android.lib.resources.oauth.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TokenResponse(
    @Json(name = "access_token")
    val accessToken: String,
    @Json(name = "expires_in")
    val expiresIn: Int,
    @Json(name = "refresh_token")
    val refreshToken: String?,
    @Json(name = "token_type")
    val tokenType: String,
    @Json(name = "user_id")
    val userId: String?,
    val scope: String?,
    @Json(name = "additional_parameters")
    val additionalParameters: Map<String, String>?
)
