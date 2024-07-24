

package com.owncloud.android.usecases.transfers.uploads

import androidx.core.net.toUri
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.owncloud.android.domain.BaseUseCase
import com.owncloud.android.domain.transfers.TransferRepository
import com.owncloud.android.extensions.getWorkInfoByTags
import com.owncloud.android.workers.UploadFileFromContentUriWorker
import timber.log.Timber

class RetryUploadFromContentUriUseCase(
    private val workManager: WorkManager,
    private val uploadFileFromContentUriUseCase: UploadFileFromContentUriUseCase,
    private val transferRepository: TransferRepository,
) : BaseUseCase<Unit, RetryUploadFromContentUriUseCase.Params>() {

    override fun run(params: Params) {
        val uploadToRetry = transferRepository.getTransferById(params.uploadIdInStorageManager)

        uploadToRetry ?: return

        val workInfos = workManager.getWorkInfoByTags(
            listOf(
                params.uploadIdInStorageManager.toString(),
                uploadToRetry.accountName,
                UploadFileFromContentUriWorker::class.java.name
            )
        )

        if (workInfos.isEmpty() || workInfos.firstOrNull()?.state == WorkInfo.State.FAILED) {
            transferRepository.updateTransferStatusToEnqueuedById(params.uploadIdInStorageManager)

            uploadFileFromContentUriUseCase(
                UploadFileFromContentUriUseCase.Params(
                    accountName = uploadToRetry.accountName,
                    contentUri = uploadToRetry.localPath.toUri(),
                    lastModifiedInSeconds = (uploadToRetry.transferEndTimestamp?.div(1000)).toString(),
                    behavior = uploadToRetry.localBehaviour.name,
                    uploadPath = uploadToRetry.remotePath,
                    uploadIdInStorageManager = params.uploadIdInStorageManager,
                    wifiOnly = false,
                    chargingOnly = false
                )
            )
        } else {
            Timber.w("Upload $uploadToRetry is already in state ${workInfos.firstOrNull()?.state}. Won't be retried")
        }
    }

    data class Params(
        val uploadIdInStorageManager: Long,
    )
}
