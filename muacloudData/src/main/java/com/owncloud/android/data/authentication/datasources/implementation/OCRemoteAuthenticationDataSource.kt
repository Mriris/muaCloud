
package com.owncloud.android.data.authentication.datasources.implementation

import com.owncloud.android.data.ClientManager
import com.owncloud.android.data.authentication.datasources.RemoteAuthenticationDataSource
import com.owncloud.android.data.executeRemoteOperation
import com.owncloud.android.data.user.datasources.implementation.toDomain
import com.owncloud.android.domain.user.model.UserInfo
import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.OwnCloudClient.WEBDAV_FILES_PATH_4_0
import com.owncloud.android.lib.common.authentication.OwnCloudCredentials
import com.owncloud.android.lib.common.authentication.OwnCloudCredentialsFactory
import com.owncloud.android.lib.resources.files.GetBaseUrlRemoteOperation
import com.owncloud.android.lib.resources.users.GetRemoteUserInfoOperation

class OCRemoteAuthenticationDataSource(
    private val clientManager: ClientManager
) : RemoteAuthenticationDataSource {
    override fun loginBasic(serverPath: String, username: String, password: String): Pair<UserInfo, String?> =
        login(OwnCloudCredentialsFactory.newBasicCredentials(username, password), serverPath)

    override fun loginOAuth(serverPath: String, username: String, accessToken: String): Pair<UserInfo, String?> =
        login(OwnCloudCredentialsFactory.newBearerCredentials(username, accessToken), serverPath)

    private fun login(ownCloudCredentials: OwnCloudCredentials, serverPath: String): Pair<UserInfo, String?> {

        val client: OwnCloudClient =
            clientManager.getClientForAnonymousCredentials(
                path = serverPath,
                requiresNewClient = false
            ).apply { credentials = ownCloudCredentials }

        val getBaseUrlRemoteOperation = GetBaseUrlRemoteOperation()
        val rawBaseUrl = executeRemoteOperation { getBaseUrlRemoteOperation.execute(client) }

        val userBaseUri = rawBaseUrl?.replace(WEBDAV_FILES_PATH_4_0, "")
            ?: client.baseUri.toString()

        lateinit var userInfo: UserInfo

        executeRemoteOperation {
            GetRemoteUserInfoOperation().execute(client)
        }.let { userInfo = it.toDomain() }

        return Pair(userInfo, userBaseUri)
    }
}
