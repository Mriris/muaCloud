

package com.owncloud.android.domain.transfers.usecases

import com.owncloud.android.domain.BaseUseCase
import com.owncloud.android.domain.transfers.TransferRepository

class UpdatePendingUploadsPathUseCase(
    private val transferRepository: TransferRepository,
) : BaseUseCase<Unit, UpdatePendingUploadsPathUseCase.Params>() {

    override fun run(params: Params) {
        transferRepository.clearSuccessfulTransfers()
        val storedUploads = transferRepository.getAllTransfers()
        storedUploads.forEach { upload ->
            transferRepository.updateTransferStorageDirectoryInLocalPath(upload.id!!, params.oldDirectory, params.newDirectory)
        }
    }

    data class Params(
        val oldDirectory: String,
        val newDirectory: String
    )
}
