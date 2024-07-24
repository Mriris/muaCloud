
package com.owncloud.android.data.user.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.owncloud.android.data.ProviderMeta.ProviderTableMeta.USER_QUOTAS_TABLE_NAME


@Entity(tableName = USER_QUOTAS_TABLE_NAME)
data class UserQuotaEntity(
    @PrimaryKey
    val accountName: String,
    val used: Long,
    val available: Long
)
