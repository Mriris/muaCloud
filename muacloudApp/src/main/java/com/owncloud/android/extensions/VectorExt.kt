

package com.owncloud.android.extensions

import com.owncloud.android.domain.files.model.OCFile
import java.util.ArrayList
import java.util.Locale
import java.util.Vector

fun Vector<OCFile>.filterByQuery(query: String): List<OCFile> {
    val lowerCaseQuery = query.lowercase(Locale.ROOT)

    val filteredList: MutableList<OCFile> = ArrayList()

    for (fileToAdd in this) {
        val nameOfTheFileToAdd: String = fileToAdd.fileName.lowercase(Locale.ROOT)
        if (nameOfTheFileToAdd.contains(lowerCaseQuery)) {
            filteredList.add(fileToAdd)
        }
    }

    // Remove not matching files from this filelist
    for (i in this.indices.reversed()) {
        if (!filteredList.contains(this[i])) {
            removeAt(i)
        }
    }

    // Add matching files to this filelist
    for (i in filteredList.indices) {
        if (!contains(filteredList[i])) {
            add(i, filteredList[i])
        }
    }

    return this
}
