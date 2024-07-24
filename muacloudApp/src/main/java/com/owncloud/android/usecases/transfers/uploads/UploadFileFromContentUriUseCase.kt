

package com.owncloud.android.usecases.transfers.uploads

import android.net.Uri
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.owncloud.android.domain.BaseUseCase
import com.owncloud.android.workers.UploadFileFromContentUriWorker
import timber.log.Timber

class UploadFileFromContentUriUseCase(
    private val workManager: WorkManager
) : BaseUseCase<Unit, UploadFileFromContentUriUseCase.Params>() {

    override fun run(params: Params) {
        val inputData = workDataOf(
            UploadFileFromContentUriWorker.KEY_PARAM_ACCOUNT_NAME to params.accountName,
            UploadFileFromContentUriWorker.KEY_PARAM_BEHAVIOR to params.behavior,
            UploadFileFromContentUriWorker.KEY_PARAM_CONTENT_URI to params.contentUri.toString(),
            UploadFileFromContentUriWorker.KEY_PARAM_LAST_MODIFIED to params.lastModifiedInSeconds,
            UploadFileFromContentUriWorker.KEY_PARAM_UPLOAD_PATH to params.uploadPath,
            UploadFileFromContentUriWorker.KEY_PARAM_UPLOAD_ID to params.uploadIdInStorageManager
        )

        val networkRequired = if (params.wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(networkRequired)
            .setRequiresCharging(params.chargingOnly)
            .build()

        val uploadFileFromContentUriWorker = OneTimeWorkRequestBuilder<UploadFileFromContentUriWorker>()
            .setInputData(inputData)
            .setConstraints(constraints)
            .addTag(params.accountName)
            .addTag(params.uploadIdInStorageManager.toString())
            .build()

        workManager.enqueue(uploadFileFromContentUriWorker)
        Timber.i("Plain upload of ${params.contentUri.path} has been enqueued.")
    }

    data class Params(
        val accountName: String,
        val contentUri: Uri,
        val lastModifiedInSeconds: String,
        val behavior: String,
        val uploadPath: String,
        val uploadIdInStorageManager: Long,
        val wifiOnly: Boolean,
        val chargingOnly: Boolean,
    )
}
