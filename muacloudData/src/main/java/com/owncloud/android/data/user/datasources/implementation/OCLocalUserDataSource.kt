

package com.owncloud.android.data.user.datasources.implementation

import androidx.annotation.VisibleForTesting
import com.owncloud.android.data.user.datasources.LocalUserDataSource
import com.owncloud.android.data.user.db.UserDao
import com.owncloud.android.data.user.db.UserQuotaEntity
import com.owncloud.android.domain.user.model.UserQuota

class OCLocalUserDataSource(
    private val userDao: UserDao
) : LocalUserDataSource {

    override fun saveQuotaForAccount(accountName: String, userQuota: UserQuota) =
        userDao.insertOrReplace(userQuota.toEntity())

    override fun getQuotaForAccount(accountName: String): UserQuota? =
        userDao.getQuotaForAccount(accountName = accountName)?.toModel()

    override fun getAllUserQuotas(): List<UserQuota> {
        return userDao.getAllUserQuotas().map { userQuotaEntity ->
            userQuotaEntity.toModel()
        }
    }

    override fun deleteQuotaForAccount(accountName: String) {
        userDao.deleteQuotaForAccount(accountName = accountName)
    }

    companion object {
        @VisibleForTesting
        fun UserQuotaEntity.toModel(): UserQuota =
            UserQuota(
                accountName = accountName,
                available = available,
                used = used
            )

        @VisibleForTesting
        fun UserQuota.toEntity(): UserQuotaEntity =
            UserQuotaEntity(
                accountName = accountName,
                available = available,
                used = used
            )
    }
}
