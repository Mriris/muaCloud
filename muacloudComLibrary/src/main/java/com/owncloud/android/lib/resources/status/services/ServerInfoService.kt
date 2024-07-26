package com.owncloud.android.lib.resources.status.services


import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.resources.status.RemoteServerInfo

interface ServerInfoService {
    fun checkPathExistence(path: String, isUserLoggedIn: Boolean, client: OwnCloudClient): RemoteOperationResult<Boolean>

    fun getRemoteStatus(path: String, client: OwnCloudClient): RemoteOperationResult<RemoteServerInfo>
}
