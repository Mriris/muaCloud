

package com.owncloud.android.domain.transfers.usecases

import com.owncloud.android.domain.BaseUseCase
import com.owncloud.android.domain.transfers.TransferRepository
import com.owncloud.android.domain.transfers.model.OCTransfer

class GetAllTransfersUseCase(
    private val transferRepository: TransferRepository,
) : BaseUseCase<List<OCTransfer>, Unit>() {
    override fun run(params: Unit): List<OCTransfer> =
        transferRepository.getAllTransfers()

}
