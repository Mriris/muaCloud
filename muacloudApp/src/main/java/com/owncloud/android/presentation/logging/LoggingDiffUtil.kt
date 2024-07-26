
package com.owncloud.android.presentation.logging

import androidx.recyclerview.widget.DiffUtil
import java.io.File

class LoggingDiffUtil(private val oldList: List<File>, private val newList: List<File>) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldItemPosition == newItemPosition

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldList[oldItemPosition].name === newList[newItemPosition].name

}
