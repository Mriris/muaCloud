

package com.owncloud.android.domain.camerauploads.model

data class FolderBackUpConfiguration(
    val accountName: String,
    val behavior: UploadBehavior,
    val sourcePath: String,
    val uploadPath: String,
    val wifiOnly: Boolean,
    val chargingOnly: Boolean,
    val lastSyncTimestamp: Long,
    val name: String,
    val spaceId: String?,
) {

    val isPictureUploads get() = name == pictureUploadsName
    val isVideoUploads get() = name == videoUploadsName

    companion object {
        const val pictureUploadsName = "Picture uploads"
        const val videoUploadsName = "Video uploads"
    }
}


enum class UploadBehavior {
    MOVE, COPY;

    @Deprecated("Legacy Local Behavior. Remove asap")
    fun toLegacyLocalBehavior(): Int {
        return when (this) {
            MOVE -> LEGACY_LOCAL_BEHAVIOUR_MOVE
            COPY -> LEGACY_LOCAL_BEHAVIOUR_COPY
        }
    }

    companion object {
        private const val LEGACY_LOCAL_BEHAVIOUR_COPY = 0
        private const val LEGACY_LOCAL_BEHAVIOUR_MOVE = 1

        fun fromString(string: String): UploadBehavior {
            return if (string.equals("MOVE", ignoreCase = true)) {
                MOVE
            } else {
                COPY
            }
        }
    }
}
