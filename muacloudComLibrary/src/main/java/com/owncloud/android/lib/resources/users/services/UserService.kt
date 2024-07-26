
package com.owncloud.android.lib.resources.users.services

import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.resources.Service
import com.owncloud.android.lib.resources.users.GetRemoteUserQuotaOperation
import com.owncloud.android.lib.resources.users.RemoteAvatarData
import com.owncloud.android.lib.resources.users.RemoteUserInfo

interface UserService : Service {
    fun getUserInfo(): RemoteOperationResult<RemoteUserInfo>
    fun getUserQuota(): RemoteOperationResult<GetRemoteUserQuotaOperation.RemoteQuota>
    fun getUserAvatar(avatarDimension: Int): RemoteOperationResult<RemoteAvatarData>
}
