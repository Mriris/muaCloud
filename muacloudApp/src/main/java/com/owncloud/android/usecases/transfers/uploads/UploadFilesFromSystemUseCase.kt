

package com.owncloud.android.usecases.transfers.uploads

import com.owncloud.android.domain.BaseUseCase
import com.owncloud.android.domain.camerauploads.model.UploadBehavior
import com.owncloud.android.domain.transfers.TransferRepository
import com.owncloud.android.domain.transfers.model.OCTransfer
import com.owncloud.android.domain.transfers.model.TransferStatus
import com.owncloud.android.domain.transfers.model.UploadEnqueuedBy
import timber.log.Timber
import java.io.File


class UploadFilesFromSystemUseCase(
    private val uploadFileFromSystemUseCase: UploadFileFromSystemUseCase,
    private val transferRepository: TransferRepository,
) : BaseUseCase<Unit, UploadFilesFromSystemUseCase.Params>() {

    override fun run(params: Params) {
        params.listOfLocalPaths.forEach { localPath ->
            val localFile = File(localPath)

            if (!localFile.exists()) {
                Timber.w("Upload of $localPath won't be enqueued. We were not able to find it in the local storage")
                return@forEach
            }

            val uploadId = storeInUploadsDatabase(
                localFile = localFile,
                uploadPath = params.uploadFolderPath.plus(localFile.name),
                accountName = params.accountName,
                spaceId = params.spaceId,
            )

            enqueueSingleUpload(
                localPath = localFile.absolutePath,
                uploadPath = params.uploadFolderPath.plus(localFile.name),
                lastModifiedInSeconds = localFile.lastModified().div(1_000).toString(),
                accountName = params.accountName,
                uploadIdInStorageManager = uploadId,
            )
        }
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
            localBehaviour = UploadBehavior.MOVE,
            forceOverwrite = false,
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
    ) {
        val uploadFileParams = UploadFileFromSystemUseCase.Params(
            accountName = accountName,
            localPath = localPath,
            lastModifiedInSeconds = lastModifiedInSeconds,
            behavior = UploadBehavior.MOVE.toString(),
            uploadPath = uploadPath,
            uploadIdInStorageManager = uploadIdInStorageManager
        )
        uploadFileFromSystemUseCase(uploadFileParams)
    }

    data class Params(
        val accountName: String,
        val listOfLocalPaths: List<String>,
        val uploadFolderPath: String,
        val spaceId: String?,
    )
}
