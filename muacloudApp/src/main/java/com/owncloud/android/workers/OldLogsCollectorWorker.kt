
package com.owncloud.android.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.owncloud.android.data.providers.LocalStorageProvider
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.util.Calendar
import java.util.concurrent.TimeUnit

class OldLogsCollectorWorker(
    val appContext: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(
    appContext,
    workerParameters
), KoinComponent {

    override suspend fun doWork(): Result {
        val logsDirectory = getLogsDirectory()
        val logsFiles = getLogsFiles(logsDirectory)

        return try {
            removeOldLogs(logsFiles)
            Result.success()
        } catch (ioException: IOException) {
            Result.failure()
        } catch (securityException: SecurityException) {
            Result.failure()
        }
    }

    private fun getLogsDirectory(): File {
        val localStorageProvider: LocalStorageProvider by inject()
        val logsPath = localStorageProvider.getLogsPath()
        return File(logsPath)
    }

    private fun getLogsFiles(logsFolder: File): List<File> {
        return logsFolder.listFiles()?.toList() ?: listOf()
    }

    private fun removeOldLogs(logFiles: List<File>) {
        logFiles.forEach { log ->
            if (log.lastModified() < getLastTimestampAllowed()) {
                Timber.i("Removing log: ${log.name}")
                log.delete()
            }
        }
    }

    private fun getLastTimestampAllowed(): Long {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DATE, -numberOfDaysToKeepLogs)

        return calendar.timeInMillis
    }

    companion object {
        const val OLD_LOGS_COLLECTOR_WORKER = "OLD_LOGS_COLLECTOR_WORKER"
        const val repeatInterval: Long = 7L
        val repeatIntervalTimeUnit: TimeUnit = TimeUnit.DAYS
        private const val numberOfDaysToKeepLogs = 7
    }
}
