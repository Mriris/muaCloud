
package com.owncloud.android.presentation.files

import androidx.annotation.DrawableRes
import com.owncloud.android.R

enum class ViewType {
    VIEW_TYPE_GRID, VIEW_TYPE_LIST;

    fun getOppositeViewType(): ViewType =
        when (this) {
            VIEW_TYPE_LIST -> VIEW_TYPE_GRID
            VIEW_TYPE_GRID -> VIEW_TYPE_LIST
        }

    @DrawableRes
    fun toDrawableRes(): Int =
        when (this) {
            VIEW_TYPE_LIST -> R.drawable.ic_baseline_view_list
            VIEW_TYPE_GRID -> R.drawable.ic_baseline_view_grid
        }
}
