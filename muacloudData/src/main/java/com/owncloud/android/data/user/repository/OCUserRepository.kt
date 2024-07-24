

package com.owncloud.android.data.user.repository

import com.owncloud.android.data.user.datasources.LocalUserDataSource
import com.owncloud.android.data.user.datasources.RemoteUserDataSource
import com.owncloud.android.domain.user.UserRepository
import com.owncloud.android.domain.user.model.UserAvatar
import com.owncloud.android.domain.user.model.UserInfo
import com.owncloud.android.domain.user.model.UserQuota

class OCUserRepository(
    private val localUserDataSource: LocalUserDataSource,
    private val remoteUserDataSource: RemoteUserDataSource
) : UserRepository {
    override fun getUserInfo(accountName: String): UserInfo = remoteUserDataSource.getUserInfo(accountName)
    override fun getUserQuota(accountName: String): UserQuota =
        remoteUserDataSource.getUserQuota(accountName).also {
            localUserDataSource.saveQuotaForAccount(accountName, it)
        }

    override fun getStoredUserQuota(accountName: String): UserQuota? =
        localUserDataSource.getQuotaForAccount(accountName)

    override fun getAllUserQuotas(): List<UserQuota> =
        localUserDataSource.getAllUserQuotas()

    override fun getUserAvatar(accountName: String): UserAvatar =
        remoteUserDataSource.getUserAvatar(accountName)
}
