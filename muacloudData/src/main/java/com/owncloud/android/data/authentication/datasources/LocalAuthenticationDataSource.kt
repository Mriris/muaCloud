

package com.owncloud.android.data.authentication.datasources

import com.owncloud.android.domain.authentication.oauth.model.ClientRegistrationInfo
import com.owncloud.android.domain.server.model.ServerInfo
import com.owncloud.android.domain.user.model.UserInfo

interface LocalAuthenticationDataSource {
    fun addBasicAccount(
        userName: String,
        lastPermanentLocation: String?,
        password: String,
        serverInfo: ServerInfo,
        userInfo: UserInfo,
        updateAccountWithUsername: String?
    ): String

    fun addOAuthAccount(
        userName: String,
        lastPermanentLocation: String?,
        authTokenType: String,
        accessToken: String,
        serverInfo: ServerInfo,
        userInfo: UserInfo,
        refreshToken: String,
        scope: String?,
        updateAccountWithUsername: String?,
        clientRegistrationInfo: ClientRegistrationInfo?
    ): String

    fun supportsOAuth2(accountName: String): Boolean

    fun getBaseUrl(accountName: String): String
}
