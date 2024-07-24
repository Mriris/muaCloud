

package com.owncloud.android.domain.server

import com.owncloud.android.domain.server.model.ServerInfo

interface ServerInfoRepository {
    fun getServerInfo(path: String, creatingAccount: Boolean): ServerInfo
}
