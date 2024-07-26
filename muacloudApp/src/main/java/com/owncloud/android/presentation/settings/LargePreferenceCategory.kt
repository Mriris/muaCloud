
package com.owncloud.android.presentation.settings

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceViewHolder

class LargePreferenceCategory @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : PreferenceCategory(context, attrs) {

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val summary = holder.findViewById(android.R.id.summary) as? TextView
        summary?.apply {
            isSingleLine = false  // Enable multiple line support
            maxLines = 3
        }
    }
}
