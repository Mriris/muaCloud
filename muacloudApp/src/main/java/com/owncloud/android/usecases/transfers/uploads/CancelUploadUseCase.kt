

package com.owncloud.android.usecases.transfers.uploads

import androidx.work.WorkManager
import com.owncloud.android.data.providers.LocalStorageProvider
import com.owncloud.android.domain.BaseUseCase
import com.owncloud.android.domain.transfers.TransferRepository
import com.owncloud.android.domain.transfers.model.OCTransfer
import com.owncloud.android.extensions.getWorkInfoByTags
import com.owncloud.android.workers.UploadFileFromContentUriWorker
import com.owncloud.android.workers.UploadFileFromFileSystemWorker
import timber.log.Timber

class CancelUploadUseCase(
    private val workManager: WorkManager,
    private val transferRepository: TransferRepository,
    private val localStorageProvider: LocalStorageProvider,
) : BaseUseCase<Unit, CancelUploadUseCase.Params>() {

    override fun run(params: Params) {
        val upload = params.upload

        val workersFromContentUriToCancel = workManager.getWorkInfoByTags(
            listOf(
                upload.id.toString(),
                upload.accountName,
                UploadFileFromContentUriWorker::class.java.name
            )
        )

        val workersFromFileSystemToCancel = workManager.getWorkInfoByTags(
            listOf(
                upload.id.toString(),
                upload.accountName,
                UploadFileFromFileSystemWorker::class.java.name
            )
        )

        val workersToCancel = workersFromContentUriToCancel + workersFromFileSystemToCancel

        workersToCancel.forEach {
            workManager.cancelWorkById(it.id)
            Timber.i("Upload with id ${upload.id} has been cancelled.")
        }

        localStorageProvider.deleteCacheIfNeeded(upload)

        transferRepository.deleteTransferById(upload.id!!)
    }

    data class Params(
        val upload: OCTransfer,
    )
}
