

package com.owncloud.android.extensions

import android.view.Menu
import com.owncloud.android.R
import com.owncloud.android.domain.files.model.FileMenuOption

fun Menu.filterMenuOptions(
    optionsToShow: List<FileMenuOption>,
    hasWritePermission: Boolean,
) {
    FileMenuOption.values().forEach { fileMenuOption ->
        val item = this.findItem(fileMenuOption.toResId())
        item?.let {
            if (optionsToShow.contains(fileMenuOption)) {
                it.isVisible = true
                it.isEnabled = true
                if (fileMenuOption.toResId() == R.id.action_open_file_with) {
                    if (!hasWritePermission) {
                        item.setTitle(R.string.actionbar_open_with_read_only)
                    } else {
                        item.setTitle(R.string.actionbar_open_with)
                    }
                }
            } else {
                it.isVisible = false
                it.isEnabled = false
            }
        }

    }
}
