

package com.owncloud.android.extensions

import android.accounts.Account
import androidx.lifecycle.LiveData
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkQuery
import com.owncloud.android.domain.files.model.OCFile
import com.owncloud.android.usecases.transfers.TRANSFER_TAG_DOWNLOAD

val PENDING_WORK_STATUS = listOf(WorkInfo.State.ENQUEUED, WorkInfo.State.RUNNING, WorkInfo.State.BLOCKED)
val FINISHED_WORK_STATUS = listOf(WorkInfo.State.SUCCEEDED, WorkInfo.State.FAILED, WorkInfo.State.CANCELLED)


fun WorkManager.getWorkInfoByTags(tags: List<String>): List<WorkInfo> =
    this.getWorkInfos(buildWorkQuery(tags = tags)).get().filter { it.tags.containsAll(tags) }


fun WorkManager.getRunningWorkInfosByTags(tags: List<String>): List<WorkInfo> {
    return getWorkInfos(buildWorkQuery(tags = tags, states = listOf(WorkInfo.State.RUNNING))).get().filter { it.tags.containsAll(tags) }
}


fun WorkManager.getRunningWorkInfosLiveData(tags: List<String>): LiveData<List<WorkInfo>> {
    return getWorkInfosLiveData(buildWorkQuery(tags = tags, states = listOf(WorkInfo.State.RUNNING)))
}


fun WorkManager.isDownloadPending(account: Account, file: OCFile): Boolean =
    this.getWorkInfoByTags(getTagsForDownload(file, account.name)).any { !it.state.isFinished }


fun WorkManager.isUploadPending(account: Account, file: OCFile): Boolean = false
fun getTagsForDownload(file: OCFile, accountName: String) =
    listOf(TRANSFER_TAG_DOWNLOAD, file.id.toString(), accountName)


fun buildWorkQuery(
    tags: List<String>,
    states: List<WorkInfo.State> = listOf(),
): WorkQuery = WorkQuery.Builder
    .fromTags(tags)
    .addStates(states)
    .build()
