
package com.owncloud.android.data.authentication.repository

import com.owncloud.android.data.authentication.datasources.LocalAuthenticationDataSource
import com.owncloud.android.data.authentication.datasources.RemoteAuthenticationDataSource
import com.owncloud.android.domain.authentication.AuthenticationRepository
import com.owncloud.android.domain.authentication.oauth.model.ClientRegistrationInfo
import com.owncloud.android.domain.server.model.ServerInfo
import com.owncloud.android.domain.user.model.UserInfo

class OCAuthenticationRepository(
    private val localAuthenticationDataSource: LocalAuthenticationDataSource,
    private val remoteAuthenticationDataSource: RemoteAuthenticationDataSource
) : AuthenticationRepository {
    override fun loginBasic(
        serverInfo: ServerInfo,
        username: String,
        password: String,
        updateAccountWithUsername: String?
    ): String {
        val userInfoAndRedirectionPath: Pair<UserInfo, String?> =
            remoteAuthenticationDataSource.loginBasic(
                serverPath = serverInfo.baseUrl,
                username = username,
                password = password
            )

        return localAuthenticationDataSource.addBasicAccount(
            userName = username,
            lastPermanentLocation = userInfoAndRedirectionPath.second,
            password = password,
            serverInfo = serverInfo,
            userInfo = userInfoAndRedirectionPath.first,
            updateAccountWithUsername = updateAccountWithUsername
        )
    }

    override fun loginOAuth(
        serverInfo: ServerInfo,
        username: String,
        authTokenType: String,
        accessToken: String,
        refreshToken: String,
        scope: String?,
        updateAccountWithUsername: String?,
        clientRegistrationInfo: ClientRegistrationInfo?
    ): String {
        val userInfoAndRedirectionPath: Pair<UserInfo, String?> =
            remoteAuthenticationDataSource.loginOAuth(
                serverPath = serverInfo.baseUrl,
                username = username,
                accessToken = accessToken
            )

        return localAuthenticationDataSource.addOAuthAccount(
            userName = username.ifBlank { userInfoAndRedirectionPath.first.id },
            lastPermanentLocation = userInfoAndRedirectionPath.second,
            authTokenType = authTokenType,
            accessToken = accessToken,
            serverInfo = serverInfo,
            userInfo = userInfoAndRedirectionPath.first,
            refreshToken = refreshToken,
            scope = scope,
            updateAccountWithUsername = updateAccountWithUsername,
            clientRegistrationInfo = clientRegistrationInfo
        )
    }

    override fun supportsOAuth2UseCase(accountName: String): Boolean =
        localAuthenticationDataSource.supportsOAuth2(accountName)

    override fun getBaseUrl(accountName: String): String = localAuthenticationDataSource.getBaseUrl(accountName)
}
