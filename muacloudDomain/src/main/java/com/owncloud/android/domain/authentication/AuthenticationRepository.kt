

package com.owncloud.android.domain.authentication

import com.owncloud.android.domain.authentication.oauth.model.ClientRegistrationInfo
import com.owncloud.android.domain.server.model.ServerInfo

interface AuthenticationRepository {
    fun loginBasic(
        serverInfo: ServerInfo,
        username: String,
        password: String,
        updateAccountWithUsername: String?
    ): String

    fun loginOAuth(
        serverInfo: ServerInfo,
        username: String,
        authTokenType: String,
        accessToken: String,
        refreshToken: String,
        scope: String?,
        updateAccountWithUsername: String?,
        clientRegistrationInfo: ClientRegistrationInfo?
    ): String

    fun supportsOAuth2UseCase(accountName: String): Boolean

    fun getBaseUrl(accountName: String): String
}
