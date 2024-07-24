

package com.owncloud.android.presentation.files.filelist

import android.util.SparseBooleanArray
import androidx.recyclerview.widget.RecyclerView

abstract class SelectableAdapter<VH : RecyclerView.ViewHolder?> :
    RecyclerView.Adapter<VH>() {
    private val selectedItems: SparseBooleanArray = SparseBooleanArray()


    fun isSelected(position: Int): Boolean {
        return getSelectedItems().contains(position)
    }


    fun toggleSelection(position: Int) {
        if (selectedItems[position, false]) {
            selectedItems.delete(position)
        } else {
            selectedItems.put(position, true)
        }
        notifyItemChanged(position)
    }


    fun clearSelection() {
        selectedItems.clear()
        notifyDataSetChanged()
    }


    val selectedItemCount: Int
        get() = selectedItems.size()


    fun getSelectedItems(): List<Int> {
        val items: MutableList<Int> = ArrayList(selectedItems.size())
        for (i in 0 until selectedItems.size()) {
            items.add(selectedItems.keyAt(i))
        }
        return items
    }


    fun toggleSelectionInBulk(totalItems: Int) {
        for (i in 0 until totalItems) {
            if (selectedItems[i, false]) {
                selectedItems.delete(i)
            } else {
                selectedItems.put(i, true)
            }
        }
        notifyDataSetChanged()
    }

    fun selectAll(totalItems: Int) {
        for (i in 0 until totalItems) {
            selectedItems.put(i, true)
        }
        notifyDataSetChanged()
    }
}
