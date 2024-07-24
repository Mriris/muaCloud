
package com.owncloud.android.domain.availableoffline.model


enum class AvailableOfflineStatus {

    NOT_AVAILABLE_OFFLINE,


    AVAILABLE_OFFLINE,


    AVAILABLE_OFFLINE_PARENT;

    companion object {
        fun fromValue(value: Int?): AvailableOfflineStatus {
            return when (value) {
                AVAILABLE_OFFLINE.ordinal -> AVAILABLE_OFFLINE
                AVAILABLE_OFFLINE_PARENT.ordinal -> AVAILABLE_OFFLINE_PARENT
                else -> NOT_AVAILABLE_OFFLINE
            }
        }
    }
}
