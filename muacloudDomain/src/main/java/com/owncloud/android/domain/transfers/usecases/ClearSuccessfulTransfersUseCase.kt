

package com.owncloud.android.domain.transfers.usecases

import com.owncloud.android.domain.BaseUseCase
import com.owncloud.android.domain.transfers.TransferRepository

class ClearSuccessfulTransfersUseCase(
    private val transferRepository: TransferRepository,
) : BaseUseCase<Unit, Unit>() {
    override fun run(params: Unit): Unit =
        transferRepository.clearSuccessfulTransfers()

}
