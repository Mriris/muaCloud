
package com.owncloud.android.data.authentication.datasources

import com.owncloud.android.domain.user.model.UserInfo

interface RemoteAuthenticationDataSource {
    // Returns a Pair with UserInfo and last permanent redirection
    fun loginBasic(serverPath: String, username: String, password: String): Pair<UserInfo, String?>
    fun loginOAuth(serverPath: String, username: String, accessToken: String): Pair<UserInfo, String?>
}
