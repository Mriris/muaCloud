

package com.owncloud.android.domain.authentication.oauth.model

data class ClientRegistrationRequest(
    val registrationEndpoint: String,
    val clientName: String,
    val redirectUris: List<String>,
    val tokenEndpointAuthMethod: String = CLIENT_REGISTRATION_AUTH_METHOD,
    val applicationType: String = CLIENT_REGISTRATION_APPLICATION_TYPE
) {

    companion object {
        
        private const val CLIENT_REGISTRATION_AUTH_METHOD = "client_secret_basic"
        private const val CLIENT_REGISTRATION_APPLICATION_TYPE = "native"
    }
}
