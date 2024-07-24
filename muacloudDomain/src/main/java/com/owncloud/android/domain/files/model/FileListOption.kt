
package com.owncloud.android.domain.files.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class FileListOption : Parcelable {
    ALL_FILES, SPACES_LIST, SHARED_BY_LINK, AV_OFFLINE;

    fun isAllFiles() = this == ALL_FILES
    fun isSpacesList() = this == SPACES_LIST
    fun isSharedByLink() = this == SHARED_BY_LINK
    fun isAvailableOffline() = this == AV_OFFLINE
}
