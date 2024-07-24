

package com.owncloud.android.usecases.transfers.downloads

import androidx.work.WorkManager
import com.owncloud.android.domain.BaseUseCase
import com.owncloud.android.domain.files.model.OCFile
import com.owncloud.android.extensions.getWorkInfoByTags
import com.owncloud.android.workers.DownloadFileWorker
import timber.log.Timber


class CancelDownloadForFileUseCase(
    private val workManager: WorkManager
) : BaseUseCase<Unit, CancelDownloadForFileUseCase.Params>() {

    override fun run(params: Params) {
        val file = params.file

        val workersToCancel = workManager.getWorkInfoByTags(
            listOf(
                file.id.toString(),
                file.owner,
                DownloadFileWorker::class.java.name,
            )
        )

        workersToCancel.forEach {
            workManager.cancelWorkById(it.id)
            Timber.i("Download with id ${file.id} has been cancelled.")
        }
    }

    data class Params(
        val file: OCFile
    )
}
