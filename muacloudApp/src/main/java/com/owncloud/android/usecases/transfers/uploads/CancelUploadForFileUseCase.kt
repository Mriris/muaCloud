

package com.owncloud.android.usecases.transfers.uploads

import androidx.work.WorkManager
import com.owncloud.android.domain.BaseUseCase
import com.owncloud.android.domain.files.model.OCFile
import com.owncloud.android.domain.transfers.TransferRepository
import com.owncloud.android.extensions.getWorkInfoByTags
import timber.log.Timber


class CancelUploadForFileUseCase(
    private val workManager: WorkManager,
    private val transferRepository: TransferRepository,
) : BaseUseCase<Unit, CancelUploadForFileUseCase.Params>() {

    override fun run(params: Params) {
        val file = params.file


        val uploadForFile = transferRepository.getCurrentAndPendingTransfers().firstOrNull {
            file.owner == it.accountName && file.remotePath == it.remotePath
        }

        if (uploadForFile == null) {
            Timber.w("Didn't found any pending upload to cancel for file ${file.remotePath} and owner ${file.owner}")
            return
        }

        val workersToCancel =
            workManager.getWorkInfoByTags(listOf(uploadForFile.id.toString(), file.owner))

        workersToCancel.forEach {




            workManager.cancelWorkById(it.id)
        }
    }

    data class Params(
        val file: OCFile
    )
}
