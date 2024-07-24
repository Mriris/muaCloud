

package com.owncloud.android.domain.authentication.oauth.model

data class ClientRegistrationInfo(
    val clientId: String,
    val clientSecret: String?,
    val clientIdIssuedAt: Int?,
    val clientSecretExpiration: Int,
)
