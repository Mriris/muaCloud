
package com.owncloud.android.presentation.files

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.owncloud.android.R
import com.owncloud.android.data.providers.SharedPreferencesProvider
import com.owncloud.android.data.providers.implementation.OCSharedPreferencesProvider
import com.owncloud.android.databinding.SortOptionsLayoutBinding
import com.owncloud.android.presentation.files.SortOrder.Companion.PREF_FILE_LIST_SORT_ORDER
import com.owncloud.android.presentation.files.SortType.Companion.PREF_FILE_LIST_SORT_TYPE

class SortOptionsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ConstraintLayout(context, attrs, defStyle) {

    var onSortOptionsListener: SortOptionsListener? = null
    var onCreateFolderListener: CreateFolderListener? = null

    private var _binding: SortOptionsLayoutBinding? = null
    private val binding get() = _binding!!

    var viewTypeSelected: ViewType = ViewType.VIEW_TYPE_LIST
        set(viewType) {
            binding.viewTypeSelector.setImageDrawable(ContextCompat.getDrawable(context, viewType.getOppositeViewType().toDrawableRes()))
            field = viewType
        }

    var sortTypeSelected: SortType = SortType.SORT_TYPE_BY_NAME
        set(sortType) {
            if (field == sortType) {

                sortOrderSelected = sortOrderSelected.getOppositeSortOrder()
            }
            binding.sortTypeTitle.text = context.getText(sortType.toStringRes())
            field = sortType
        }

    var sortOrderSelected: SortOrder = SortOrder.SORT_ORDER_ASCENDING
        set(sortOrder) {
            binding.sortTypeIcon.setImageDrawable(ContextCompat.getDrawable(context, sortOrder.toDrawableRes()))
            field = sortOrder
        }

    init {
        _binding = SortOptionsLayoutBinding.inflate(LayoutInflater.from(context), this, true)

        val sharedPreferencesProvider: SharedPreferencesProvider = OCSharedPreferencesProvider(context)

        sortTypeSelected = SortType.values()[sharedPreferencesProvider.getInt(PREF_FILE_LIST_SORT_TYPE, SortType.SORT_TYPE_BY_NAME.ordinal)]
        sortOrderSelected = SortOrder.values()[sharedPreferencesProvider.getInt(PREF_FILE_LIST_SORT_ORDER, SortOrder.SORT_ORDER_ASCENDING.ordinal)]

        binding.sortTypeSelector.setOnClickListener {
            onSortOptionsListener?.onSortTypeListener(
                sortTypeSelected,
                sortOrderSelected
            )
        }
        binding.viewTypeSelector.setOnClickListener {
            onSortOptionsListener?.onViewTypeListener(
                viewTypeSelected.getOppositeViewType()
            )
        }
    }

    fun selectAdditionalView(additionalView: AdditionalView) {
        when (additionalView) {
            AdditionalView.CREATE_FOLDER -> {
                binding.viewTypeSelector.apply {
                    visibility = VISIBLE
                    setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_action_create_dir))
                    setOnClickListener {
                        onCreateFolderListener?.onCreateFolderListener()
                    }
                }
            }
            AdditionalView.VIEW_TYPE -> {
                viewTypeSelected = viewTypeSelected
                binding.viewTypeSelector.apply {
                    visibility = VISIBLE
                    setOnClickListener {
                        onSortOptionsListener?.onViewTypeListener(
                            viewTypeSelected.getOppositeViewType()
                        )
                    }
                }
            }
            AdditionalView.HIDDEN -> {
                binding.viewTypeSelector.visibility = INVISIBLE
            }
        }
    }

    interface SortOptionsListener {
        fun onSortTypeListener(sortType: SortType, sortOrder: SortOrder)
        fun onViewTypeListener(viewType: ViewType)
    }

    interface CreateFolderListener {
        fun onCreateFolderListener()
    }

    enum class AdditionalView {
        CREATE_FOLDER, VIEW_TYPE, HIDDEN
    }
}
