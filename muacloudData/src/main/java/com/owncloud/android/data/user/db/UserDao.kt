

package com.owncloud.android.data.user.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.owncloud.android.data.ProviderMeta

@Dao
interface UserDao {
    @Query(SELECT_QUOTA)
    fun getQuotaForAccount(
        accountName: String
    ): UserQuotaEntity?

    @Query(SELECT_ALL_QUOTAS)
    fun getAllUserQuotas(): List<UserQuotaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplace(userQuotaEntity: UserQuotaEntity)

    @Query(DELETE_QUOTA)
    fun deleteQuotaForAccount(accountName: String)

    companion object {
        private const val SELECT_QUOTA = """
            SELECT *
            FROM ${ProviderMeta.ProviderTableMeta.USER_QUOTAS_TABLE_NAME}
            WHERE accountName = :accountName
        """

        private const val SELECT_ALL_QUOTAS = """
            SELECT *
            FROM ${ProviderMeta.ProviderTableMeta.USER_QUOTAS_TABLE_NAME}
        """

        private const val DELETE_QUOTA = """
            DELETE
            FROM ${ProviderMeta.ProviderTableMeta.USER_QUOTAS_TABLE_NAME}
            WHERE accountName = :accountName
        """
    }
}
