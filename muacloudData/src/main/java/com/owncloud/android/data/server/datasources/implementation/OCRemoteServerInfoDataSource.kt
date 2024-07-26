

package com.owncloud.android.data.server.datasources.implementation

import com.owncloud.android.data.ClientManager
import com.owncloud.android.data.executeRemoteOperation
import com.owncloud.android.data.server.datasources.RemoteServerInfoDataSource
import com.owncloud.android.domain.exceptions.OwncloudVersionNotSupportedException
import com.owncloud.android.domain.exceptions.SpecificServiceUnavailableException
import com.owncloud.android.domain.server.model.AuthenticationMethod
import com.owncloud.android.domain.server.model.ServerInfo
import com.owncloud.android.lib.common.http.HttpConstants
import com.owncloud.android.lib.common.network.WebdavUtils.normalizeProtocolPrefix
import com.owncloud.android.lib.resources.status.RemoteServerInfo
import com.owncloud.android.lib.resources.status.services.ServerInfoService

class OCRemoteServerInfoDataSource(
    private val serverInfoService: ServerInfoService,
    private val clientManager: ClientManager
) : RemoteServerInfoDataSource {

    fun getAuthenticationMethod(path: String): AuthenticationMethod {

        val owncloudClient = clientManager.getClientForAnonymousCredentials(path, false)

        val checkPathExistenceResult =
            serverInfoService.checkPathExistence(path = path, isUserLoggedIn = false, client = owncloudClient)

        if (checkPathExistenceResult.httpCode == HttpConstants.HTTP_SERVICE_UNAVAILABLE) {
            throw SpecificServiceUnavailableException(checkPathExistenceResult.httpPhrase)
        }

        var authenticationMethod = AuthenticationMethod.NONE
        if (checkPathExistenceResult.httpCode == HttpConstants.HTTP_UNAUTHORIZED) {
            val authenticateHeaders = checkPathExistenceResult.authenticateHeaders
            var isBasic = false
            authenticateHeaders.forEach { authenticateHeader ->
                if (authenticateHeader.contains(AuthenticationMethod.BEARER_TOKEN.toString())) {
                    return AuthenticationMethod.BEARER_TOKEN  // Bearer top priority
                } else if (authenticateHeader.contains(AuthenticationMethod.BASIC_HTTP_AUTH.toString())) {
                    isBasic = true
                }
            }

            if (isBasic) {
                authenticationMethod = AuthenticationMethod.BASIC_HTTP_AUTH
            }
        }

        return authenticationMethod
    }

    fun getRemoteStatus(path: String): RemoteServerInfo {
        val ownCloudClient = clientManager.getClientForAnonymousCredentials(path, true)

        val remoteStatusResult = serverInfoService.getRemoteStatus(path, ownCloudClient)

        val remoteServerInfo = executeRemoteOperation {
            remoteStatusResult
        }

        if (!remoteServerInfo.ownCloudVersion.isServerVersionSupported && !remoteServerInfo.ownCloudVersion.isVersionHidden) {
            throw OwncloudVersionNotSupportedException()
        }

        return remoteServerInfo
    }

    override fun getServerInfo(path: String): ServerInfo {

        val remoteServerInfo = getRemoteStatus(path)
        val normalizedProtocolPrefix =
            normalizeProtocolPrefix(remoteServerInfo.baseUrl, remoteServerInfo.isSecureConnection)

        val authenticationMethod = getAuthenticationMethod(normalizedProtocolPrefix)

        return if (authenticationMethod == AuthenticationMethod.BEARER_TOKEN) {
            ServerInfo.OAuth2Server(
                ownCloudVersion = remoteServerInfo.ownCloudVersion.version,
                baseUrl = normalizedProtocolPrefix
            )
        } else {
            ServerInfo.BasicServer(
                ownCloudVersion = remoteServerInfo.ownCloudVersion.version,
                baseUrl = normalizedProtocolPrefix,
            )
        }
    }
}
