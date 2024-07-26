
package com.owncloud.android.lib.resources.users.services.implementation

import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.resources.users.GetRemoteUserAvatarOperation
import com.owncloud.android.lib.resources.users.GetRemoteUserInfoOperation
import com.owncloud.android.lib.resources.users.GetRemoteUserQuotaOperation
import com.owncloud.android.lib.resources.users.RemoteAvatarData
import com.owncloud.android.lib.resources.users.RemoteUserInfo
import com.owncloud.android.lib.resources.users.services.UserService

class OCUserService(override val client: OwnCloudClient) : UserService {
    override fun getUserInfo(): RemoteOperationResult<RemoteUserInfo> =
        GetRemoteUserInfoOperation().execute(client)

    override fun getUserQuota(): RemoteOperationResult<GetRemoteUserQuotaOperation.RemoteQuota> =
        GetRemoteUserQuotaOperation().execute(client)

    override fun getUserAvatar(avatarDimension: Int): RemoteOperationResult<RemoteAvatarData> =
        GetRemoteUserAvatarOperation(avatarDimension).execute(client)

}
