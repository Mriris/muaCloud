
package com.owncloud.android.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.owncloud.android.domain.availableoffline.usecases.GetFilesAvailableOfflineFromEveryAccountUseCase
import com.owncloud.android.domain.files.model.OCFile
import com.owncloud.android.usecases.synchronization.SynchronizeFileUseCase
import com.owncloud.android.usecases.synchronization.SynchronizeFolderUseCase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.util.concurrent.TimeUnit

class AvailableOfflinePeriodicWorker(
    val appContext: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(
    appContext,
    workerParameters
), KoinComponent {

    private val getFilesAvailableOfflineFromEveryAccountUseCase: GetFilesAvailableOfflineFromEveryAccountUseCase by inject()
    private val synchronizeFileUseCase: SynchronizeFileUseCase by inject()
    private val synchronizeFolderUseCase: SynchronizeFolderUseCase by inject()

    override suspend fun doWork(): Result {

        return try {
            val availableOfflineFiles = getFilesAvailableOfflineFromEveryAccountUseCase(Unit)
            Timber.i("Available offline files that needs to be synced: ${availableOfflineFiles.size}")

            syncAvailableOfflineFiles(availableOfflineFiles)

            Result.success()
        } catch (exception: Exception) {
            Result.failure()
        }
    }

    private fun syncAvailableOfflineFiles(availableOfflineFiles: List<OCFile>) {
        availableOfflineFiles.forEach {
            if (it.isFolder) {
                synchronizeFolderUseCase(
                    SynchronizeFolderUseCase.Params(
                        remotePath = it.remotePath,
                        accountName = it.owner,
                        spaceId = it.spaceId,
                        syncMode = SynchronizeFolderUseCase.SyncFolderMode.SYNC_FOLDER_RECURSIVELY
                    )
                )
            } else {
                synchronizeFileUseCase(SynchronizeFileUseCase.Params(it))
            }
        }
    }

    companion object {
        const val AVAILABLE_OFFLINE_PERIODIC_WORKER = "AVAILABLE_OFFLINE_PERIODIC_WORKER"
        const val repeatInterval: Long = 15L
        val repeatIntervalTimeUnit: TimeUnit = TimeUnit.MINUTES
    }
}
