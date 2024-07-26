
package com.owncloud.android.lib.resources.shares

import com.owncloud.android.lib.resources.shares.responses.ItemType


data class RemoteShare(
    var id: String = "0",
    var shareWith: String = "",
    var path: String = "",
    var token: String = "",
    var itemType: String = "",
    var sharedWithDisplayName: String = "",
    var sharedWithAdditionalInfo: String = "",
    var name: String = "",
    var shareLink: String = "",
    var shareType: ShareType? = ShareType.UNKNOWN,
    var permissions: Int = DEFAULT_PERMISSION,
    var sharedDate: Long = INIT_SHARED_DATE,
    var expirationDate: Long = INIT_EXPIRATION_DATE_IN_MILLIS,
    var isFolder: Boolean = (itemType == ItemType.FOLDER.fileValue)
) {

    companion object {
        const val DEFAULT_PERMISSION = -1
        const val READ_PERMISSION_FLAG = 1
        const val UPDATE_PERMISSION_FLAG = 2
        const val CREATE_PERMISSION_FLAG = 4
        const val DELETE_PERMISSION_FLAG = 8
        const val SHARE_PERMISSION_FLAG = 16
        const val MAXIMUM_PERMISSIONS_FOR_FILE = READ_PERMISSION_FLAG +
                UPDATE_PERMISSION_FLAG +
                SHARE_PERMISSION_FLAG
        const val MAXIMUM_PERMISSIONS_FOR_FOLDER = MAXIMUM_PERMISSIONS_FOR_FILE +
                CREATE_PERMISSION_FLAG +
                DELETE_PERMISSION_FLAG
        const val FEDERATED_PERMISSIONS_FOR_FILE = READ_PERMISSION_FLAG +
                UPDATE_PERMISSION_FLAG +
                SHARE_PERMISSION_FLAG
        const val FEDERATED_PERMISSIONS_FOR_FOLDER = READ_PERMISSION_FLAG +
                UPDATE_PERMISSION_FLAG +
                CREATE_PERMISSION_FLAG +
                DELETE_PERMISSION_FLAG +
                SHARE_PERMISSION_FLAG

        const val INIT_EXPIRATION_DATE_IN_MILLIS: Long = 0
        const val INIT_SHARED_DATE: Long = 0
    }
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
