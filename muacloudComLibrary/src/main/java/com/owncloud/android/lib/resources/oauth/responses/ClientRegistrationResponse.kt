package com.owncloud.android.lib.resources.oauth.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ClientRegistrationResponse(
    @Json(name = "client_id")
    val clientId: String,
    @Json(name = "client_secret")
    val clientSecret: String?,
    @Json(name = "client_id_issued_at")
    val clientIdIssuedAt: Int?,
    @Json(name = "client_secret_expires_at")
    val clientSecretExpiration: Int,
)
