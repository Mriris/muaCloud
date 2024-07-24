

package com.owncloud.android.usecases.transfers.uploads

import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.owncloud.android.domain.BaseUseCase
import com.owncloud.android.workers.UploadFileFromFileSystemWorker
import timber.log.Timber

class UploadFileFromSystemUseCase(
    private val workManager: WorkManager
) : BaseUseCase<Unit, UploadFileFromSystemUseCase.Params>() {

    override fun run(params: Params) {
        val inputData = workDataOf(
            UploadFileFromFileSystemWorker.KEY_PARAM_ACCOUNT_NAME to params.accountName,
            UploadFileFromFileSystemWorker.KEY_PARAM_BEHAVIOR to params.behavior,
            UploadFileFromFileSystemWorker.KEY_PARAM_LOCAL_PATH to params.localPath,
            UploadFileFromFileSystemWorker.KEY_PARAM_LAST_MODIFIED to params.lastModifiedInSeconds,
            UploadFileFromFileSystemWorker.KEY_PARAM_UPLOAD_PATH to params.uploadPath,
            UploadFileFromFileSystemWorker.KEY_PARAM_UPLOAD_ID to params.uploadIdInStorageManager
        )

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val uploadFileFromSystemWorker = OneTimeWorkRequestBuilder<UploadFileFromFileSystemWorker>()
            .setInputData(inputData)
            .setConstraints(constraints)
            .addTag(params.accountName)
            .addTag(params.uploadIdInStorageManager.toString())
            .build()

        workManager.enqueue(uploadFileFromSystemWorker)
        Timber.i("Plain upload of ${params.localPath} has been enqueued.")
    }

    data class Params(
        val accountName: String,
        val localPath: String,
        val lastModifiedInSeconds: String,
        val behavior: String,
        val uploadPath: String,
        val uploadIdInStorageManager: Long,
    )
}
