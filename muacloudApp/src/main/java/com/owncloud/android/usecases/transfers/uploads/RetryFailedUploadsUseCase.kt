

package com.owncloud.android.usecases.transfers.uploads

import android.content.Context
import com.owncloud.android.domain.BaseUseCase
import com.owncloud.android.domain.transfers.TransferRepository
import com.owncloud.android.extensions.isContentUri
import timber.log.Timber

class RetryFailedUploadsUseCase(
    private val context: Context,
    private val retryUploadFromContentUriUseCase: RetryUploadFromContentUriUseCase,
    private val retryUploadFromSystemUseCase: RetryUploadFromSystemUseCase,
    private val transferRepository: TransferRepository,
) : BaseUseCase<Unit, Unit>() {

    override fun run(params: Unit) {
        val failedUploads = transferRepository.getFailedTransfers()

        if (failedUploads.isEmpty()) {
            Timber.d("There are no failed uploads to retry.")
            return
        }
        failedUploads.forEach { upload ->
            if (upload.isContentUri(context)) {
                retryUploadFromContentUriUseCase(RetryUploadFromContentUriUseCase.Params(upload.id!!))
            } else {
                retryUploadFromSystemUseCase(RetryUploadFromSystemUseCase.Params(upload.id!!))
            }
        }
    }
}
