
package com.owncloud.android.presentation.logging

import androidx.lifecycle.ViewModel
import com.owncloud.android.data.providers.LocalStorageProvider
import java.io.File

class LogListViewModel(
    private val localStorageProvider: LocalStorageProvider
) : ViewModel() {

    private fun getLogsDirectory(): File {
        val logsPath = localStorageProvider.getLogsPath()
        return File(logsPath)
    }

    fun getLogsFiles(): List<File> {
        return getLogsDirectory().listFiles()?.toList()?.sortedBy { it.name } ?: listOf()
    }
}
