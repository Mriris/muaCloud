
package com.owncloud.android.usecases.transfers.downloads

import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.owncloud.android.domain.BaseUseCase
import com.owncloud.android.domain.files.model.OCFile
import com.owncloud.android.extensions.PENDING_WORK_STATUS
import com.owncloud.android.extensions.buildWorkQuery
import com.owncloud.android.extensions.getTagsForDownload
import com.owncloud.android.usecases.transfers.MAXIMUM_NUMBER_OF_RETRIES
import com.owncloud.android.usecases.transfers.TRANSFER_TAG_DOWNLOAD
import com.owncloud.android.workers.DownloadFileWorker
import timber.log.Timber
import java.util.UUID


class DownloadFileUseCase(
    private val workManager: WorkManager
) : BaseUseCase<UUID?, DownloadFileUseCase.Params>() {

    override fun run(params: Params): UUID? {
        val ocFile = params.file
        val accountName = params.accountName

        if (ocFile.id == null) return null

        if (isDownloadAlreadyEnqueued(accountName, ocFile)) {
            return null
        }

        return enqueueNewDownload(ocFile, accountName)
    }

    private fun isDownloadAlreadyEnqueued(accountName: String, file: OCFile): Boolean {
        val tagsToFilter = getTagsForDownload(file, accountName)
        val workQuery = buildWorkQuery(
            tags = tagsToFilter,
            states = PENDING_WORK_STATUS,
        )

        val downloadWorkersForFile =
            workManager.getWorkInfos(workQuery).get().filter { it.tags.containsAll(tagsToFilter) }

        var isEnqueued = false
        downloadWorkersForFile.forEach {
            // Let's cancel a work if it has several retries and enqueue it again
            if (it.runAttemptCount > MAXIMUM_NUMBER_OF_RETRIES) {
                workManager.cancelWorkById(it.id)
            } else {
                isEnqueued = true
            }
        }

        if (isEnqueued) {
            Timber.i("Download of ${file.fileName} has not finished yet. Do not enqueue it again.")
        }

        return isEnqueued
    }

    private fun enqueueNewDownload(ocFile: OCFile, accountName: String): UUID {
        val inputData = workDataOf(
            DownloadFileWorker.KEY_PARAM_ACCOUNT to accountName,
            DownloadFileWorker.KEY_PARAM_FILE_ID to ocFile.id
        )

        val downloadFileWork = OneTimeWorkRequestBuilder<DownloadFileWorker>()
            .setInputData(inputData)
            .addTag(ocFile.id.toString())
            .addTag(accountName)
            .addTag(TRANSFER_TAG_DOWNLOAD)
            .build()

        workManager.enqueue(downloadFileWork)
        Timber.i("Download of ${ocFile.fileName} has been enqueued.")

        return downloadFileWork.id
    }

    data class Params(
        val accountName: String,
        val file: OCFile
    )
}
