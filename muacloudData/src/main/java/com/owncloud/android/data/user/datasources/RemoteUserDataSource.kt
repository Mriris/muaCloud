

package com.owncloud.android.data.user.datasources

import com.owncloud.android.domain.user.model.UserAvatar
import com.owncloud.android.domain.user.model.UserInfo
import com.owncloud.android.domain.user.model.UserQuota

interface RemoteUserDataSource {
    fun getUserInfo(accountName: String): UserInfo
    fun getUserQuota(accountName: String): UserQuota
    fun getUserAvatar(accountName: String): UserAvatar
}
