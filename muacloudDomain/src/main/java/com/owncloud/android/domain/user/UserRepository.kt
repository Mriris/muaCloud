

package com.owncloud.android.domain.user

import com.owncloud.android.domain.user.model.UserAvatar
import com.owncloud.android.domain.user.model.UserInfo
import com.owncloud.android.domain.user.model.UserQuota

interface UserRepository {
    fun getUserInfo(accountName: String): UserInfo
    fun getUserQuota(accountName: String): UserQuota
    fun getStoredUserQuota(accountName: String): UserQuota?
    fun getAllUserQuotas(): List<UserQuota>
    fun getUserAvatar(accountName: String): UserAvatar
}
