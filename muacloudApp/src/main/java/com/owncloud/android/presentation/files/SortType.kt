
package com.owncloud.android.presentation.files

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.owncloud.android.R
import com.owncloud.android.utils.FileStorageUtils
import kotlinx.parcelize.Parcelize

@Parcelize
enum class SortType : Parcelable {
    SORT_TYPE_BY_NAME, SORT_TYPE_BY_DATE, SORT_TYPE_BY_SIZE;

    @StringRes
    fun toStringRes(): Int =
        when (this) {
            SORT_TYPE_BY_NAME -> R.string.global_name
            SORT_TYPE_BY_DATE -> R.string.global_date
            SORT_TYPE_BY_SIZE -> R.string.global_size
        }

    companion object {
        const val PREF_FILE_LIST_SORT_TYPE = "PREF_FILE_LIST_SORT_TYPE"

        fun fromPreference(value: Int): SortType =
            when (value) {
                FileStorageUtils.SORT_NAME -> SORT_TYPE_BY_NAME
                FileStorageUtils.SORT_SIZE -> SORT_TYPE_BY_SIZE
                FileStorageUtils.SORT_DATE -> SORT_TYPE_BY_DATE
                else -> throw IllegalArgumentException("Sort type not supported")
            }
    }
}

@Parcelize
enum class SortOrder : Parcelable {
    SORT_ORDER_ASCENDING, SORT_ORDER_DESCENDING;

    fun getOppositeSortOrder(): SortOrder =
        when (this) {
            SORT_ORDER_ASCENDING -> SORT_ORDER_DESCENDING
            SORT_ORDER_DESCENDING -> SORT_ORDER_ASCENDING
        }

    @DrawableRes
    fun toDrawableRes(): Int =
        when (this) {
            SORT_ORDER_ASCENDING -> R.drawable.ic_baseline_arrow_upward
            SORT_ORDER_DESCENDING -> R.drawable.ic_baseline_arrow_downward
        }

    companion object {
        const val PREF_FILE_LIST_SORT_ORDER = "PREF_FILE_LIST_SORT_ORDER"

        fun fromPreference(value: Int) =
            when (value) {
                SORT_ORDER_ASCENDING.ordinal -> SORT_ORDER_ASCENDING
                SORT_ORDER_DESCENDING.ordinal -> SORT_ORDER_DESCENDING
                else -> SORT_ORDER_ASCENDING
            }
    }
}
