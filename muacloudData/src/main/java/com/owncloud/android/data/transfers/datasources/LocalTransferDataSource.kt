

package com.owncloud.android.data.transfers.datasources

import com.owncloud.android.domain.transfers.model.OCTransfer
import com.owncloud.android.domain.transfers.model.TransferResult
import com.owncloud.android.domain.transfers.model.TransferStatus
import kotlinx.coroutines.flow.Flow

interface LocalTransferDataSource {
    fun saveTransfer(transfer: OCTransfer): Long
    fun updateTransfer(transfer: OCTransfer)
    fun updateTransferStatusToInProgressById(id: Long)
    fun updateTransferStatusToEnqueuedById(id: Long)
    fun updateTransferWhenFinished(
        id: Long,
        status: TransferStatus,
        transferEndTimestamp: Long,
        lastResult: TransferResult
    )

    fun updateTransferLocalPath(id: Long, localPath: String)
    fun updateTransferStorageDirectoryInLocalPath(
        id: Long,
        oldDirectory: String,
        newDirectory: String
    )

    fun deleteTransferById(id: Long)
    fun deleteAllTransfersFromAccount(accountName: String)
    fun getTransferById(id: Long): OCTransfer?
    fun getAllTransfers(): List<OCTransfer>
    fun getAllTransfersAsStream(): Flow<List<OCTransfer>>
    fun getLastTransferFor(remotePath: String, accountName: String): OCTransfer?
    fun getCurrentAndPendingTransfers(): List<OCTransfer>
    fun getFailedTransfers(): List<OCTransfer>
    fun getFinishedTransfers(): List<OCTransfer>
    fun clearFailedTransfers()
    fun clearSuccessfulTransfers()
}
