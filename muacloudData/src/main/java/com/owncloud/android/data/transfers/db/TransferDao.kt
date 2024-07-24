

package com.owncloud.android.data.transfers.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.owncloud.android.data.ProviderMeta.ProviderTableMeta.TRANSFERS_TABLE_NAME
import kotlinx.coroutines.flow.Flow

@Dao
interface TransferDao {
    @Query(SELECT_TRANSFER_WITH_ID)
    fun getTransferWithId(id: Long): OCTransferEntity?

    @Query(SELECT_LAST_TRANSFER_WITH_REMOTE_PATH_AND_ACCOUNT_NAME)
    fun getLastTransferWithRemotePathAndAccountName(remotePath: String, accountName: String): OCTransferEntity?

    @Query(SELECT_TRANSFERS_WITH_STATUS)
    fun getTransfersWithStatus(status: List<Int>): List<OCTransferEntity>

    @Query(SELECT_ALL_TRANSFERS)
    fun getAllTransfers(): List<OCTransferEntity>

    @Query(SELECT_ALL_TRANSFERS)
    fun getAllTransfersAsStream(): Flow<List<OCTransferEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplace(ocTransferEntity: OCTransferEntity): Long

    @Query(UPDATE_TRANSFER_STATUS_WITH_ID)
    fun updateTransferStatusWithId(id: Long, newStatus: Int)

    @Query(UPDATE_TRANSFER_WHEN_FINISHED)
    fun updateTransferWhenFinished(id: Long, status: Int, transferEndTimestamp: Long, lastResult: Int)

    @Query(UPDATE_TRANSFER_LOCAL_PATH_WITH_ID)
    fun updateTransferLocalPath(id: Long, localPath: String)

    @Query(UPDATE_TRANSFER_STORAGE_DIRECTORY)
    fun updateTransferStorageDirectoryInLocalPath(id: Long, oldDirectory: String, newDirectory: String)

    @Query(DELETE_TRANSFER_WITH_ID)
    fun deleteTransferWithId(id: Long)

    @Query(DELETE_TRANSFERS_WITH_ACCOUNT_NAME)
    fun deleteTransfersWithAccountName(accountName: String)

    @Query(DELETE_TRANSFERS_WITH_STATUS)
    fun deleteTransfersWithStatus(status: Int)

    companion object {
        private const val SELECT_TRANSFER_WITH_ID = """
            SELECT *
            FROM $TRANSFERS_TABLE_NAME
            WHERE id = :id
        """

        private const val SELECT_LAST_TRANSFER_WITH_REMOTE_PATH_AND_ACCOUNT_NAME = """
            SELECT *
            FROM $TRANSFERS_TABLE_NAME
            WHERE remotePath = :remotePath AND accountName = :accountName
            ORDER BY transferEndTimestamp DESC
            LIMIT 1
        """

        private const val SELECT_TRANSFERS_WITH_STATUS = """
            SELECT *
            FROM $TRANSFERS_TABLE_NAME
            WHERE status IN (:status)
        """

        private const val SELECT_ALL_TRANSFERS = """
            SELECT *
            FROM $TRANSFERS_TABLE_NAME
        """

        private const val UPDATE_TRANSFER_STATUS_WITH_ID = """
            UPDATE $TRANSFERS_TABLE_NAME
            SET status = :newStatus
            WHERE id = :id
        """

        private const val UPDATE_TRANSFER_WHEN_FINISHED = """
            UPDATE $TRANSFERS_TABLE_NAME
            SET status = :status, transferEndTimestamp = :transferEndTimestamp, lastResult = :lastResult
            WHERE id = :id
        """

        private const val UPDATE_TRANSFER_LOCAL_PATH_WITH_ID = """
            UPDATE $TRANSFERS_TABLE_NAME
            SET localPath = :localPath
            WHERE id = :id
        """

        private const val UPDATE_TRANSFER_STORAGE_DIRECTORY = """
            UPDATE $TRANSFERS_TABLE_NAME
            SET localPath = `REPLACE`(localPath, :oldDirectory, :newDirectory)
            WHERE id = :id
        """

        private const val DELETE_TRANSFER_WITH_ID = """
            DELETE
            FROM $TRANSFERS_TABLE_NAME
            WHERE id = :id
        """

        private const val DELETE_TRANSFERS_WITH_ACCOUNT_NAME = """
            DELETE
            FROM $TRANSFERS_TABLE_NAME
            WHERE accountName = :accountName
        """

        private const val DELETE_TRANSFERS_WITH_STATUS = """
            DELETE
            FROM $TRANSFERS_TABLE_NAME
            WHERE status = :status
        """
    }
}
