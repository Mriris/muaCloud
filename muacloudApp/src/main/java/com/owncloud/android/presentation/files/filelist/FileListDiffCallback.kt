

package com.owncloud.android.presentation.files.filelist

import androidx.recyclerview.widget.DiffUtil
import com.owncloud.android.domain.files.model.FileListOption
import com.owncloud.android.domain.files.model.OCFileWithSyncInfo
import com.owncloud.android.domain.files.model.OCFooterFile

class FileListDiffCallback(
    private val oldList: List<Any>,
    private val newList: List<Any>,
    private val oldFileListOption: FileListOption,
    private val newFileListOption: FileListOption,
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]

        if (oldItem is Unit && newItem is Unit) {
            return true
        }

        if (oldItem is Boolean && newItem is Boolean) {
            return true
        }

        if (oldItem is OCFileWithSyncInfo && newItem is OCFileWithSyncInfo) {
            return oldItem.file.id == newItem.file.id
        }

        if (oldItem is OCFooterFile && newItem is OCFooterFile) {
            return oldItem.text == newItem.text
        }

        return false
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldList[oldItemPosition] == newList[newItemPosition] && oldFileListOption == newFileListOption
}
