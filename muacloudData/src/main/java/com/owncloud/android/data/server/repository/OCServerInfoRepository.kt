

package com.owncloud.android.data.server.repository

import com.owncloud.android.data.oauth.datasources.RemoteOAuthDataSource
import com.owncloud.android.data.server.datasources.RemoteServerInfoDataSource
import com.owncloud.android.data.webfinger.datasources.RemoteWebFingerDataSource
import com.owncloud.android.domain.server.ServerInfoRepository
import com.owncloud.android.domain.server.model.ServerInfo
import com.owncloud.android.domain.webfinger.model.WebFingerRel
import timber.log.Timber

class OCServerInfoRepository(
    private val remoteServerInfoDataSource: RemoteServerInfoDataSource,
    private val webFingerDatasource: RemoteWebFingerDataSource,
    private val oidcRemoteOAuthDataSource: RemoteOAuthDataSource,
) : ServerInfoRepository {

    override fun getServerInfo(path: String, creatingAccount: Boolean): ServerInfo {
        val oidcIssuerFromWebFinger: String? = if (creatingAccount) retrieveOIDCIssuerFromWebFinger(serverUrl = path) else null

        if (oidcIssuerFromWebFinger != null) {
            val openIDConnectServerConfiguration = oidcRemoteOAuthDataSource.performOIDCDiscovery(oidcIssuerFromWebFinger)
            return ServerInfo.OIDCServer(
                ownCloudVersion = "10.12",
                baseUrl = path,
                oidcServerConfiguration = openIDConnectServerConfiguration
            )
        }

        val serverInfo = remoteServerInfoDataSource.getServerInfo(path)

        if (serverInfo is ServerInfo.BasicServer) {
            return serverInfo
        } else {

            val openIDConnectServerConfiguration = try {
                oidcRemoteOAuthDataSource.performOIDCDiscovery(serverInfo.baseUrl)
            } catch (exception: Exception) {
                Timber.d("OIDC discovery not found")
                null
            }

            return if (openIDConnectServerConfiguration != null) {
                ServerInfo.OIDCServer(
                    ownCloudVersion = serverInfo.ownCloudVersion,
                    baseUrl = serverInfo.baseUrl,
                    oidcServerConfiguration = openIDConnectServerConfiguration
                )
            } else {
                ServerInfo.OAuth2Server(
                    ownCloudVersion = serverInfo.ownCloudVersion,
                    baseUrl = serverInfo.baseUrl,
                )
            }
        }
    }

    private fun retrieveOIDCIssuerFromWebFinger(
        serverUrl: String,
    ): String? {
        val oidcIssuer = try {
            webFingerDatasource.getInstancesFromWebFinger(
                lookupServer = serverUrl,
                rel = WebFingerRel.OIDC_ISSUER_DISCOVERY,
                resource = serverUrl,
            ).firstOrNull()
        } catch (exception: Exception) {
            Timber.d("Cant retrieve the oidc issuer from webfinger.")
            null
        }

        return oidcIssuer
    }
}
