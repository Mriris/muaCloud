

package com.owncloud.android.data.capabilities.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.owncloud.android.data.ProviderMeta.ProviderTableMeta

@Dao
interface OCCapabilityDao {
    @Query(SELECT)
    fun getCapabilitiesForAccountAsLiveData(
        accountName: String
    ): LiveData<OCCapabilityEntity?>

    @Query(SELECT)
    fun getCapabilitiesForAccount(
        accountName: String
    ): OCCapabilityEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplace(ocCapability: OCCapabilityEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplace(ocCapabilities: List<OCCapabilityEntity>): List<Long>

    @Query(DELETE_CAPABILITIES_BY_ACCOUNTNAME)
    fun deleteByAccountName(accountName: String)

    @Transaction
    fun replace(ocCapabilities: List<OCCapabilityEntity>) {
        ocCapabilities.forEach { ocCapability ->
            ocCapability.accountName?.run {
                deleteByAccountName(this)
            }
        }
        insertOrReplace(ocCapabilities)
    }

    companion object {
        private const val SELECT = """
            SELECT *
            FROM ${ProviderTableMeta.CAPABILITIES_TABLE_NAME}
            WHERE ${ProviderTableMeta.CAPABILITIES_ACCOUNT_NAME} = :accountName
        """
        private const val DELETE_CAPABILITIES_BY_ACCOUNTNAME = """
            DELETE
            FROM ${ProviderTableMeta.CAPABILITIES_TABLE_NAME}
            WHERE ${ProviderTableMeta.CAPABILITIES_ACCOUNT_NAME} = :accountName
        """
    }
}
