
package com.owncloud.android.domain.authentication.oauth.model

data class OIDCServerConfiguration(
    val authorizationEndpoint: String,
    val checkSessionIframe: String?,
    val endSessionEndpoint: String?,
    val issuer: String,
    val registrationEndpoint: String?,
    val responseTypesSupported: List<String>,
    val scopesSupported: List<String>?,
    val tokenEndpoint: String,
    val tokenEndpointAuthMethodsSupported: List<String>?,
    val userInfoEndpoint: String?,
)
