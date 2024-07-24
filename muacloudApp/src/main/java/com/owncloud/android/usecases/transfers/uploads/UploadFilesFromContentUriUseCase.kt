

package com.owncloud.android.usecases.transfers.uploads

import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.owncloud.android.MainApp
import com.owncloud.android.domain.BaseUseCase
import com.owncloud.android.domain.camerauploads.model.UploadBehavior
import com.owncloud.android.domain.transfers.TransferRepository
import com.owncloud.android.domain.transfers.model.OCTransfer
import com.owncloud.android.domain.transfers.model.TransferStatus
import com.owncloud.android.domain.transfers.model.UploadEnqueuedBy
import timber.log.Timber


class UploadFilesFromContentUriUseCase(
    private val uploadFileFromContentUriUseCase: UploadFileFromContentUriUseCase,
    private val transferRepository: TransferRepository,
) : BaseUseCase<Unit, UploadFilesFromContentUriUseCase.Params>() {

    override fun run(params: Params) {
        params.listOfContentUris.forEach { contentUri ->
            val documentFile = DocumentFile.fromSingleUri(MainApp.appContext.applicationContext, contentUri)

            if (documentFile == null) {
                Timber.w("Upload of $contentUri won't be enqueued. We were not able to find it in the local storage")
                return@forEach
            }

            val uploadId = storeInUploadsDatabase(
                documentFile = documentFile,
                uploadPath = params.uploadFolderPath.plus(documentFile.name),
                accountName = params.accountName,
                spaceId = params.spaceId,
            )

            enqueueSingleUpload(
                contentUri = documentFile.uri,
                uploadPath = params.uploadFolderPath.plus(documentFile.name),
                lastModifiedInSeconds = documentFile.lastModified().div(1_000).toString(),
                accountName = params.accountName,
                uploadIdInStorageManager = uploadId,
            )
        }
    }

    private fun storeInUploadsDatabase(
        documentFile: DocumentFile,
        uploadPath: String,
        accountName: String,
        spaceId: String?,
    ): Long {
        val ocTransfer = OCTransfer(
            localPath = documentFile.uri.toString(),
            remotePath = uploadPath,
            accountName = accountName,
            fileSize = documentFile.length(),
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
        contentUri: Uri,
        lastModifiedInSeconds: String,
        uploadIdInStorageManager: Long,
        uploadPath: String,
    ) {
        val uploadFileParams = UploadFileFromContentUriUseCase.Params(
            contentUri = contentUri,
            uploadPath = uploadPath,
            lastModifiedInSeconds = lastModifiedInSeconds,
            behavior = UploadBehavior.COPY.toString(),
            accountName = accountName,
            uploadIdInStorageManager = uploadIdInStorageManager,
            wifiOnly = false,
            chargingOnly = false
        )
        uploadFileFromContentUriUseCase(uploadFileParams)
    }

    data class Params(
        val accountName: String,
        val listOfContentUris: List<Uri>,
        val uploadFolderPath: String,
        val spaceId: String?,
    )
}
