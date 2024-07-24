

package com.owncloud.android.usecases.transfers.uploads

import androidx.work.WorkManager
import com.owncloud.android.domain.BaseUseCase
import com.owncloud.android.domain.transfers.TransferRepository
import timber.log.Timber


class CancelTransfersFromAccountUseCase(
    private val workManager: WorkManager,
    private val transferRepository: TransferRepository,
) : BaseUseCase<Unit, CancelTransfersFromAccountUseCase.Params>() {

    override fun run(params: Params) {
        workManager.cancelAllWorkByTag(params.accountName)

        transferRepository.deleteAllTransfersFromAccount(params.accountName)

        Timber.i("Uploads and downloads of ${params.accountName} have been cancelled.")
    }

    data class Params(
        val accountName: String,
    )
}
