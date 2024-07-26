package com.owncloud.android.lib.resources.oauth.params

import com.owncloud.android.lib.common.http.HttpConstants.CONTENT_TYPE_JSON
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

data class ClientRegistrationParams(
    val registrationEndpoint: String,
    val clientName: String,
    val redirectUris: List<String>,
    val tokenEndpointAuthMethod: String,
    val applicationType: String
) {
    fun toRequestBody(): RequestBody =
        JSONObject().apply {
            put(PARAM_APPLICATION_TYPE, applicationType)
            put(PARAM_CLIENT_NAME, clientName)
            put(PARAM_REDIRECT_URIS, JSONArray(redirectUris))
            put(PARAM_TOKEN_ENDPOINT_AUTH_METHOD, tokenEndpointAuthMethod)
        }.toString().toRequestBody(CONTENT_TYPE_JSON.toMediaType())

    companion object {
        private const val PARAM_APPLICATION_TYPE = "application_type"
        private const val PARAM_CLIENT_NAME = "client_name"
        private const val PARAM_TOKEN_ENDPOINT_AUTH_METHOD = "token_endpoint_auth_method"
        private const val PARAM_REDIRECT_URIS = "redirect_uris"
    }
}
