

package com.owncloud.android.data.user.datasources.implementation

import com.owncloud.android.data.ClientManager
import com.owncloud.android.data.executeRemoteOperation
import com.owncloud.android.data.user.datasources.RemoteUserDataSource
import com.owncloud.android.domain.user.model.UserAvatar
import com.owncloud.android.domain.user.model.UserInfo
import com.owncloud.android.domain.user.model.UserQuota
import com.owncloud.android.lib.resources.users.GetRemoteUserQuotaOperation
import com.owncloud.android.lib.resources.users.RemoteAvatarData
import com.owncloud.android.lib.resources.users.RemoteUserInfo

class OCRemoteUserDataSource(
    private val clientManager: ClientManager,
    private val avatarDimension: Int
) : RemoteUserDataSource {

    override fun getUserInfo(accountName: String): UserInfo =
        executeRemoteOperation {
            clientManager.getUserService(accountName).getUserInfo()
        }.toDomain()

    override fun getUserQuota(accountName: String): UserQuota =
        executeRemoteOperation {
            clientManager.getUserService(accountName).getUserQuota()
        }.toDomain(accountName)

    override fun getUserAvatar(accountName: String): UserAvatar =
        executeRemoteOperation {
            clientManager.getUserService(accountName = accountName).getUserAvatar(avatarDimension)
        }.toDomain()

}

/**************************************************************************************************************
 ************************************************* Mappers ****************************************************
 **************************************************************************************************************/
fun RemoteUserInfo.toDomain(): UserInfo =
    UserInfo(
        id = this.id,
        displayName = this.displayName,
        email = this.email
    )

private fun RemoteAvatarData.toDomain(): UserAvatar =
    UserAvatar(
        avatarData = this.avatarData,
        eTag = this.eTag,
        mimeType = this.mimeType
    )

private fun GetRemoteUserQuotaOperation.RemoteQuota.toDomain(accountName: String): UserQuota =
    UserQuota(
        accountName = accountName,
        available = this.free,
        used = this.used
    )
