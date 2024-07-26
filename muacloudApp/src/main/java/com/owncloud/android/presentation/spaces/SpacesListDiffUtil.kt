

package com.owncloud.android.presentation.spaces

import androidx.recyclerview.widget.DiffUtil
import com.owncloud.android.domain.spaces.model.OCSpace

class SpacesListDiffUtil(
    private val oldList: List<OCSpace>,
    private val newList: List<OCSpace>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]

        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]

        return !((oldItem.name != newItem.name) || (oldItem.description != newItem.description) ||
                (oldItem.getSpaceSpecialImage()?.id != newItem.getSpaceSpecialImage()?.id))
    }
}
