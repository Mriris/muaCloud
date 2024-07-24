

package com.owncloud.android.domain.transfers.usecases

import com.owncloud.android.domain.BaseUseCase
import com.owncloud.android.domain.transfers.TransferRepository
import com.owncloud.android.domain.transfers.model.OCTransfer
import kotlinx.coroutines.flow.Flow

class GetAllTransfersAsStreamUseCase(
    private val transferRepository: TransferRepository,
) : BaseUseCase<Flow<List<OCTransfer>>, Unit>() {
    override fun run(params: Unit): Flow<List<OCTransfer>> =
        transferRepository.getAllTransfersAsStream()

}
