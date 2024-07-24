

package com.owncloud.android.usecases.transfers.uploads

import androidx.work.WorkManager
import com.owncloud.android.data.providers.LocalStorageProvider
import com.owncloud.android.domain.BaseUseCase
import com.owncloud.android.domain.transfers.TransferRepository

class ClearFailedTransfersUseCase(
    private val workManager: WorkManager,
    private val transferRepository: TransferRepository,
    private val localStorageProvider: LocalStorageProvider,
) : BaseUseCase<Unit, Unit>() {
    override fun run(params: Unit) {
        val failedTransfers = transferRepository.getFailedTransfers()
        failedTransfers.forEach { failedTransfer ->
            workManager.cancelAllWorkByTag(failedTransfer.id.toString())
            localStorageProvider.deleteCacheIfNeeded(failedTransfer)
        }
        transferRepository.clearFailedTransfers()
    }
}
