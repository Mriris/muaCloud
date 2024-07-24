

package com.owncloud.android.usecases.transfers.uploads

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.owncloud.android.domain.BaseUseCase
import com.owncloud.android.domain.transfers.TransferRepository
import com.owncloud.android.domain.transfers.model.OCTransfer
import timber.log.Timber

class RetryFailedUploadsForAccountUseCase(
    private val context: Context,
    private val retryUploadFromContentUriUseCase: RetryUploadFromContentUriUseCase,
    private val retryUploadFromSystemUseCase: RetryUploadFromSystemUseCase,
    private val transferRepository: TransferRepository,
) : BaseUseCase<Unit, RetryFailedUploadsForAccountUseCase.Params>() {

    override fun run(params: Params) {
        val failedUploads = transferRepository.getFailedTransfers()
        val failedUploadsForAccount = failedUploads.filter { it.accountName == params.accountName }

        if (failedUploadsForAccount.isEmpty()) {
            Timber.d("There are no failed uploads for the account ${params.accountName}")
            return
        }

        failedUploadsForAccount.forEach { upload ->
            if (isContentUri(context = context, upload = upload)) {
                retryUploadFromContentUriUseCase(RetryUploadFromContentUriUseCase.Params(upload.id!!))
            } else {
                retryUploadFromSystemUseCase(RetryUploadFromSystemUseCase.Params(upload.id!!))
            }
        }
    }

    private fun isContentUri(context: Context, upload: OCTransfer): Boolean {
        return DocumentFile.isDocumentUri(context, Uri.parse(upload.localPath))
    }

    data class Params(
        val accountName: String
    )
}
