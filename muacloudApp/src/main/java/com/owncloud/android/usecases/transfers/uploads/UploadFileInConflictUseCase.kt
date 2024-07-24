

package com.owncloud.android.usecases.transfers.uploads

import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.owncloud.android.domain.BaseUseCase
import com.owncloud.android.domain.camerauploads.model.UploadBehavior
import com.owncloud.android.domain.transfers.TransferRepository
import com.owncloud.android.domain.transfers.model.OCTransfer
import com.owncloud.android.domain.transfers.model.TransferStatus
import com.owncloud.android.domain.transfers.model.UploadEnqueuedBy
import com.owncloud.android.workers.UploadFileFromFileSystemWorker
import timber.log.Timber
import java.io.File
import java.util.UUID


class UploadFileInConflictUseCase(
    private val workManager: WorkManager,
    private val transferRepository: TransferRepository,
) : BaseUseCase<UUID?, UploadFileInConflictUseCase.Params>() {

    override fun run(params: Params): UUID? {
        val localFile = File(params.localPath)

        if (!localFile.exists()) {
            Timber.w("Upload of ${params.localPath} won't be enqueued. We were not able to find it in the local storage")
            return null
        }

        Timber.d("Upload file in conflict params: ${params.accountName} | ${params.localPath} | ${params.uploadFolderPath} ")
        val uploadId = storeInUploadsDatabase(
            localFile = localFile,
            uploadPath = params.uploadFolderPath.plus(localFile.name),
            accountName = params.accountName,
            spaceId = params.spaceId,
        )

        return enqueueSingleUpload(
            localPath = localFile.absolutePath,
            uploadPath = params.uploadFolderPath.plus(localFile.name),
            lastModifiedInSeconds = localFile.lastModified().div(1_000).toString(),
            accountName = params.accountName,
            uploadIdInStorageManager = uploadId,
        )
    }

    private fun storeInUploadsDatabase(
        localFile: File,
        uploadPath: String,
        accountName: String,
        spaceId: String?,
    ): Long {
        val ocTransfer = OCTransfer(
            localPath = localFile.absolutePath,
            remotePath = uploadPath,
            accountName = accountName,
            fileSize = localFile.length(),
            status = TransferStatus.TRANSFER_QUEUED,
            localBehaviour = UploadBehavior.COPY,
            forceOverwrite = true,
            createdBy = UploadEnqueuedBy.ENQUEUED_BY_USER,
            spaceId = spaceId,
        )

        return transferRepository.saveTransfer(ocTransfer).also {
            Timber.i("Upload of $uploadPath has been stored in the uploads database with id: $it")
        }
    }

    private fun enqueueSingleUpload(
        accountName: String,
        localPath: String,
        lastModifiedInSeconds: String,
        uploadIdInStorageManager: Long,
        uploadPath: String,
    ): UUID {
        val inputData = workDataOf(
            UploadFileFromFileSystemWorker.KEY_PARAM_ACCOUNT_NAME to accountName,
            UploadFileFromFileSystemWorker.KEY_PARAM_BEHAVIOR to UploadBehavior.COPY.name,
            UploadFileFromFileSystemWorker.KEY_PARAM_LOCAL_PATH to localPath,
            UploadFileFromFileSystemWorker.KEY_PARAM_LAST_MODIFIED to lastModifiedInSeconds,
            UploadFileFromFileSystemWorker.KEY_PARAM_UPLOAD_PATH to uploadPath,
            UploadFileFromFileSystemWorker.KEY_PARAM_UPLOAD_ID to uploadIdInStorageManager,
        )

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val uploadFileFromContentUriWorker = OneTimeWorkRequestBuilder<UploadFileFromFileSystemWorker>()
            .setInputData(inputData)
            .setConstraints(constraints)
            .addTag(accountName)
            .addTag(uploadIdInStorageManager.toString())
            .build()

        workManager.enqueue(uploadFileFromContentUriWorker)
        Timber.i("Plain upload of $localPath has been enqueued.")

        return uploadFileFromContentUriWorker.id
    }

    data class Params(
        val accountName: String,
        val localPath: String,
        val uploadFolderPath: String,
        val spaceId: String?,
    )
}
