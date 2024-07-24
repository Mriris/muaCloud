
package com.owncloud.android.usecases.transfers.downloads

import android.accounts.Account
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.owncloud.android.domain.BaseUseCase
import com.owncloud.android.extensions.FINISHED_WORK_STATUS
import com.owncloud.android.extensions.buildWorkQuery
import com.owncloud.android.usecases.transfers.TRANSFER_TAG_DOWNLOAD


class GetLiveDataForFinishedDownloadsFromAccountUseCase(
    private val workManager: WorkManager
) : BaseUseCase<LiveData<List<WorkInfo>>, GetLiveDataForFinishedDownloadsFromAccountUseCase.Params>() {

    override fun run(params: Params): LiveData<List<WorkInfo>> {
        val tagsToFilter = listOf(TRANSFER_TAG_DOWNLOAD, params.account.name)
        val workQuery = buildWorkQuery(
            tags = tagsToFilter,
            states = FINISHED_WORK_STATUS
        )

        return workManager.getWorkInfosLiveData(workQuery).map { listOfDownloads ->
            listOfDownloads
                .asReversed()
                .distinctBy { it.tags }
                .filter { it.tags.containsAll(tagsToFilter) }
        }
    }

    data class Params(
        val account: Account
    )
}
