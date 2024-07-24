

package com.owncloud.android.presentation.settings.advanced

const val PREFERENCE_REMOVE_LOCAL_FILES = "remove_local_files"

enum class RemoveLocalFiles {
    NEVER, ONE_HOUR, TWELVE_HOURS, ONE_DAY, THIRTY_DAYS;

    fun toMilliseconds(): Long {
        return when (this) {
            NEVER -> -1
            ONE_HOUR -> 3_600_000
            TWELVE_HOURS -> 43_200_000
            ONE_DAY -> 86_400_000
            THIRTY_DAYS -> 2_592_000_000
        }
    }
}
