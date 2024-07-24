
package com.owncloud.android.usecases.transfers.downloads

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.owncloud.android.domain.BaseUseCase
import com.owncloud.android.domain.files.model.OCFile
import com.owncloud.android.extensions.PENDING_WORK_STATUS
import com.owncloud.android.extensions.buildWorkQuery
import com.owncloud.android.extensions.getTagsForDownload


class GetLiveDataForDownloadingFileUseCase(
    private val workManager: WorkManager
) : BaseUseCase<LiveData<WorkInfo?>, GetLiveDataForDownloadingFileUseCase.Params>() {

    override fun run(params: Params): LiveData<WorkInfo?> {
        val tagsToFilter = getTagsForDownload(params.file, params.accountName)
        val workQuery = buildWorkQuery(
            tags = tagsToFilter,
            states = PENDING_WORK_STATUS
        )

        return workManager.getWorkInfosLiveData(workQuery).map { listOfDownloads ->
            listOfDownloads.firstOrNull { it.tags.containsAll(tagsToFilter) }
        }
    }

    data class Params(
        val accountName: String,
        val file: OCFile
    )
}
