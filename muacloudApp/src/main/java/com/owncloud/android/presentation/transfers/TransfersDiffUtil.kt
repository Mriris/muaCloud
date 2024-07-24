

package com.owncloud.android.presentation.transfers

import androidx.recyclerview.widget.DiffUtil

class TransfersDiffUtil(
    private val oldList: List<TransfersAdapter.TransferRecyclerItem>,
    private val newList: List<TransfersAdapter.TransferRecyclerItem>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]

        if (oldItem is TransfersAdapter.TransferRecyclerItem.TransferItem && newItem is TransfersAdapter.TransferRecyclerItem.TransferItem) {
            return oldItem.transfer.id == newItem.transfer.id
        }

        if (oldItem is TransfersAdapter.TransferRecyclerItem.HeaderItem && newItem is TransfersAdapter.TransferRecyclerItem.HeaderItem) {
            return oldItem.status == newItem.status
        }

        return false
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldList[oldItemPosition] == newList[newItemPosition]

}
