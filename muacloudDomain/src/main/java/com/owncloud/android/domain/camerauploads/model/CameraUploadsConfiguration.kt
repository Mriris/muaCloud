
package com.owncloud.android.domain.camerauploads.model

data class CameraUploadsConfiguration(
    val pictureUploadsConfiguration: FolderBackUpConfiguration?,
    val videoUploadsConfiguration: FolderBackUpConfiguration?
) {
    fun areCameraUploadsDisabled() = pictureUploadsConfiguration == null && videoUploadsConfiguration == null
}
