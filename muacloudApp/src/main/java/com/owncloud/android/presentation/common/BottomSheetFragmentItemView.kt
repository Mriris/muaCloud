

package com.owncloud.android.presentation.common

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.owncloud.android.R
import com.owncloud.android.databinding.BottomSheetFragmentItemBinding

class BottomSheetFragmentItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ConstraintLayout(context, attrs, defStyle) {

    private var _binding: BottomSheetFragmentItemBinding? = null
    private val binding get() = _binding!!

    var itemIcon: Drawable?
        get() = binding.itemIcon.drawable
        set(value) {
            binding.itemIcon.setImageDrawable(value)
        }

    var title: CharSequence?
        get() = binding.itemTitle.text
        set(value) {
            binding.itemTitle.text = value
        }

    var itemAdditionalIcon: Drawable?
        get() = binding.itemAdditionalIcon.drawable
        set(value) {
            binding.itemAdditionalIcon.setImageDrawable(value)
        }

    init {
        _binding = BottomSheetFragmentItemBinding.inflate(LayoutInflater.from(context), this, true)

        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.BottomSheetFragmentItemView, 0, 0)
        try {
            itemIcon = a.getDrawable(R.styleable.BottomSheetFragmentItemView_itemIcon)
            title = a.getString(R.styleable.BottomSheetFragmentItemView_title)
        } finally {
            a.recycle()
        }
    }

    fun setSelected(iconAdditional: Int) {
        itemAdditionalIcon = ContextCompat.getDrawable(context, iconAdditional)
        val selectedColor = ContextCompat.getColor(context, R.color.primary)
        binding.itemIcon.setColorFilter(selectedColor)
        binding.itemTitle.setTextColor(selectedColor)
        binding.itemAdditionalIcon.setColorFilter(selectedColor)
    }

    fun removeDefaultTint() {
        binding.itemIcon.imageTintList = null
    }

    fun addDefaultTint(tintColor: Int) {
        val itemColor = ContextCompat.getColor(context, tintColor)
        val itemColorStateList = ColorStateList.valueOf(itemColor)
        binding.itemIcon.imageTintList = itemColorStateList
    }
}
