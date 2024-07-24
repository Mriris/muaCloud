

package com.owncloud.android.usecases.transfers.uploads

import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.owncloud.android.domain.BaseUseCase
import com.owncloud.android.domain.camerauploads.model.UploadBehavior
import com.owncloud.android.domain.transfers.TransferRepository
import com.owncloud.android.extensions.getWorkInfoByTags
import com.owncloud.android.workers.UploadFileFromFileSystemWorker
import timber.log.Timber

class RetryUploadFromSystemUseCase(
    private val workManager: WorkManager,
    private val uploadFileFromSystemUseCase: UploadFileFromSystemUseCase,
    private val transferRepository: TransferRepository,
) : BaseUseCase<Unit, RetryUploadFromSystemUseCase.Params>() {

    override fun run(params: Params) {
        val uploadToRetry = transferRepository.getTransferById(params.uploadIdInStorageManager)

        uploadToRetry ?: return

        val workInfos = workManager.getWorkInfoByTags(
            listOf(
                params.uploadIdInStorageManager.toString(),
                uploadToRetry.accountName,
                UploadFileFromFileSystemWorker::class.java.name
            )
        )

        if (workInfos.isEmpty() || workInfos.firstOrNull()?.state == WorkInfo.State.FAILED) {
            transferRepository.updateTransferStatusToEnqueuedById(params.uploadIdInStorageManager)

            uploadFileFromSystemUseCase(
                UploadFileFromSystemUseCase.Params(
                    accountName = uploadToRetry.accountName,
                    localPath = uploadToRetry.localPath,
                    lastModifiedInSeconds = (uploadToRetry.transferEndTimestamp?.div(1000)).toString(),
                    behavior = UploadBehavior.MOVE.name,
                    uploadPath = uploadToRetry.remotePath,
                    uploadIdInStorageManager = params.uploadIdInStorageManager
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
