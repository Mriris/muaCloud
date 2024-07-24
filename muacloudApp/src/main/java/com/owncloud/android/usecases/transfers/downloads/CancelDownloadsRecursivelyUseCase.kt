

package com.owncloud.android.usecases.transfers.downloads

import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.owncloud.android.domain.BaseUseCase
import com.owncloud.android.domain.files.model.OCFile
import com.owncloud.android.domain.files.usecases.GetFolderContentUseCase
import com.owncloud.android.extensions.getWorkInfoByTags
import com.owncloud.android.workers.DownloadFileWorker
import timber.log.Timber


class CancelDownloadsRecursivelyUseCase(
    private val workManager: WorkManager,
    private val getFolderContentUseCase: GetFolderContentUseCase,
) : BaseUseCase<Unit, CancelDownloadsRecursivelyUseCase.Params>() {

    private lateinit var downloadsWorkInfos: List<WorkInfo>

    override fun run(params: Params) {
        downloadsWorkInfos = workManager.getWorkInfoByTags(
            listOf(
                params.accountName,
                DownloadFileWorker::class.java.name
            )
        )

        val files = params.files
        files.forEach { file ->
            cancelRecursively(file)
        }
    }

    private fun cancelRecursively(file: OCFile) {
        if (file.isFolder) {
            val result = getFolderContentUseCase(GetFolderContentUseCase.Params(file.id!!))
            val files = result.getDataOrNull()
            files?.forEach { fileInFolder ->
                cancelRecursively(fileInFolder)
            }
        } else {
            val workersToCancel = downloadsWorkInfos.filter { it.tags.contains(file.id.toString()) }

            workersToCancel.forEach {
                workManager.cancelWorkById(it.id)
                Timber.i("Download with id ${file.id} has been cancelled.")
            }
        }
    }

    data class Params(
        val files: List<OCFile>,
        val accountName: String
    )
}
