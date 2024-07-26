
package com.owncloud.android.lib.resources.status.services.implementation

import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.resources.files.CheckPathExistenceRemoteOperation
import com.owncloud.android.lib.resources.status.GetRemoteStatusOperation
import com.owncloud.android.lib.resources.status.RemoteServerInfo
import com.owncloud.android.lib.resources.status.services.ServerInfoService

class OCServerInfoService : ServerInfoService {

    override fun checkPathExistence(
        path: String,
        isUserLoggedIn: Boolean,
        client: OwnCloudClient,
    ): RemoteOperationResult<Boolean> =
        CheckPathExistenceRemoteOperation(
            remotePath = path,
            isUserLoggedIn = isUserLoggedIn,
        ).execute(client)

    override fun getRemoteStatus(
        path: String,
        client: OwnCloudClient,
    ): RemoteOperationResult<RemoteServerInfo> =
        GetRemoteStatusOperation().execute(client)
}
