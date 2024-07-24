

package com.owncloud.android.domain.sharing.shares.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class OCShare(
    val id: Int? = null,
    val shareType: ShareType,
    val shareWith: String?,
    val path: String,
    val permissions: Int,
    val sharedDate: Long,
    val expirationDate: Long,
    val token: String?,
    val sharedWithDisplayName: String?,
    val sharedWithAdditionalInfo: String?,
    val isFolder: Boolean,
    val remoteId: String,
    var accountOwner: String = "",
    val name: String?,
    val shareLink: String?
) : Parcelable {

    val isPasswordProtected: Boolean
        get() = shareType == ShareType.PUBLIC_LINK && !shareWith.isNullOrEmpty()
}



enum class ShareType constructor(val value: Int) {
    UNKNOWN(-1),
    USER(0),
    GROUP(1),
    PUBLIC_LINK(3),
    EMAIL(4),
    CONTACT(5),
    FEDERATED(6);

    companion object {
        fun fromValue(value: Int) = values().firstOrNull { it.value == value }
    }
}
