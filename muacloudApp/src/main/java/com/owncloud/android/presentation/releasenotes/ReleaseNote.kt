

package com.owncloud.android.presentation.releasenotes

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.owncloud.android.R

data class ReleaseNote(
    @StringRes val title: Int,
    @StringRes val subtitle: Int,
    val type: ReleaseNoteType
)

enum class ReleaseNoteType(@DrawableRes val drawableRes: Int) {
    BUGFIX(R.drawable.ic_release_notes_healing),
    CHANGE(R.drawable.ic_release_notes_autorenew),
    ENHANCEMENT(R.drawable.ic_release_notes_architecture),
    SECURITY(R.drawable.ic_lock)
}
