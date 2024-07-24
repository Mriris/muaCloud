
package com.owncloud.android.utils

import com.owncloud.android.domain.files.model.OCFile
import com.owncloud.android.presentation.files.SortType

class SortFilesUtils {
    fun sortFiles(
        listOfFiles: List<OCFile>,
        sortTypeValue: Int,
        ascending: Boolean,
    ): List<OCFile> {
        return when (SortType.fromPreference(sortTypeValue)) {
            SortType.SORT_TYPE_BY_NAME -> sortByName(listOfFiles, ascending)
            SortType.SORT_TYPE_BY_SIZE -> sortBySize(listOfFiles, ascending)
            SortType.SORT_TYPE_BY_DATE -> sortByDate(listOfFiles, ascending)
        }
    }

    private fun sortByName(listOfFiles: List<OCFile>, ascending: Boolean): List<OCFile> {
        val newListOfFiles =
            if (ascending) listOfFiles.sortedBy { it.fileName.lowercase() }
            else listOfFiles.sortedByDescending { it.fileName.lowercase() }

        // Show first the folders when sorting by name
        return newListOfFiles.sortedByDescending { it.isFolder }
    }

    private fun sortBySize(listOfFiles: List<OCFile>, ascending: Boolean): List<OCFile> {
        return if (ascending) listOfFiles.sortedBy { it.length }
        else listOfFiles.sortedByDescending { it.length }
    }

    private fun sortByDate(listOfFiles: List<OCFile>, ascending: Boolean): List<OCFile> {
        return if (ascending) listOfFiles.sortedBy { it.modificationTimestamp }
        else listOfFiles.sortedByDescending { it.modificationTimestamp }
    }
}
