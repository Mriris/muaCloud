package com.owncloud.android.lib.resources.oauth.responses

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OIDCDiscoveryResponse(
    val authorization_endpoint: String,
    val check_session_iframe: String?,
    val end_session_endpoint: String?,
    val issuer: String,
    val registration_endpoint: String?,
    val response_types_supported: List<String>,
    val scopes_supported: List<String>?,
    val token_endpoint: String,
    val token_endpoint_auth_methods_supported: List<String>?,
    val userinfo_endpoint: String?,
)
