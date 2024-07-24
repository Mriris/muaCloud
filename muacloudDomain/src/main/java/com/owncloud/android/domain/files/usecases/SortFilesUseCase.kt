
package com.owncloud.android.domain.files.usecases

import com.owncloud.android.domain.BaseUseCase
import com.owncloud.android.domain.files.model.OCFile

class SortFilesUseCase : BaseUseCase<List<OCFile>, SortFilesUseCase.Params>() {

    override fun run(params: Params): List<OCFile> {
        return when (params.sortType) {
            SortType.SORT_BY_NAME -> sortByName(params.listOfFiles, params.ascending)
            SortType.SORT_BY_SIZE -> sortBySize(params.listOfFiles, params.ascending)
            SortType.SORT_BY_DATE -> sortByDate(params.listOfFiles, params.ascending)
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

    data class Params(
        val listOfFiles: List<OCFile>,
        val sortType: SortType,
        val ascending: Boolean,
    )
}

enum class SortType {
    SORT_BY_NAME, SORT_BY_SIZE, SORT_BY_DATE;

    companion object {
        fun fromPreferences(preferenceValue: Int): SortType {
            return when (preferenceValue) {
                0 -> SORT_BY_NAME
                1 -> SORT_BY_DATE
                2 -> SORT_BY_SIZE
                else -> throw IllegalStateException("Sort type not expected")
            }
        }
    }
}
