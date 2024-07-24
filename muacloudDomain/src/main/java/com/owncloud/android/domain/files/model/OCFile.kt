

package com.owncloud.android.domain.files.model

import android.os.Parcelable
import android.webkit.MimeTypeMap
import com.owncloud.android.domain.availableoffline.model.AvailableOfflineStatus
import com.owncloud.android.domain.availableoffline.model.AvailableOfflineStatus.AVAILABLE_OFFLINE
import com.owncloud.android.domain.availableoffline.model.AvailableOfflineStatus.AVAILABLE_OFFLINE_PARENT
import com.owncloud.android.domain.extensions.isOneOf
import kotlinx.parcelize.Parcelize
import java.io.File
import java.util.Locale

@Parcelize
data class OCFile(
    var id: Long? = null,
    var parentId: Long? = null,
    val owner: String,
    var length: Long,
    var creationTimestamp: Long? = 0,
    var modificationTimestamp: Long,
    val remotePath: String,
    var mimeType: String,
    var etag: String? = "",
    val permissions: String? = null,
    var remoteId: String? = null,
    val privateLink: String? = "",
    var storagePath: String? = null,
    var treeEtag: String? = "",
    var availableOfflineStatus: AvailableOfflineStatus? = null,
    var lastSyncDateForData: Long? = 0,
    var lastUsage: Long? = null,
    var needsToUpdateThumbnail: Boolean = false,
    var modifiedAtLastSyncForData: Long? = 0,
    var etagInConflict: String? = null,
    val fileIsDownloading: Boolean? = false,
    var sharedWithSharee: Boolean? = false,
    var sharedByLink: Boolean = false,
    val spaceId: String? = null,
) : Parcelable {

    val fileName: String
        get() = File(remotePath).name.let { it.ifBlank { ROOT_PATH } }

    @Deprecated("Do not use this constructor. Remove it as soon as possible")
    constructor(remotePath: String, mimeType: String, parentId: Long?, owner: String, spaceId: String? = null) : this(
        remotePath = remotePath,
        mimeType = mimeType,
        parentId = parentId,
        owner = owner,
        spaceId = spaceId,
        modificationTimestamp = 0,
        length = 0
    )


    val isFolder
        get() = mimeType.isOneOf(MIME_DIR, MIME_DIR_UNIX)


    val isAudio: Boolean
        get() = isOfType(MIME_PREFIX_AUDIO)


    val isVideo: Boolean
        get() = isOfType(MIME_PREFIX_VIDEO)


    val isImage: Boolean
        get() = isOfType(MIME_PREFIX_IMAGE)


    val isText: Boolean
        get() = isOfType(MIME_PREFIX_TEXT)


    val hasWritePermission: Boolean
        get() = permissions?.contains(char = 'W', ignoreCase = true) ?: false


    val hasDeletePermission: Boolean
        get() = permissions?.contains(char = 'D', ignoreCase = true) ?: false


    val hasRenamePermission: Boolean
        get() = permissions?.contains(char = 'N', ignoreCase = true) ?: false


    val hasMovePermission: Boolean
        get() = permissions?.contains(char = 'V', ignoreCase = true) ?: false


    val hasAddFilePermission: Boolean
        get() = permissions?.contains(char = 'C', ignoreCase = true) ?: false


    val hasAddSubdirectoriesPermission: Boolean
        get() = permissions?.contains(char = 'K', ignoreCase = true) ?: false


    val hasResharePermission: Boolean
        get() = permissions?.contains(char = 'R', ignoreCase = true) ?: false


    fun getParentRemotePath(): String {
        val parentPath: String = File(remotePath).parent ?: throw IllegalArgumentException("Parent path is null")
        return if (parentPath.endsWith("$PATH_SEPARATOR")) parentPath else "$parentPath$PATH_SEPARATOR"
    }


    val isAvailableLocally: Boolean
        get() =
            storagePath?.takeIf {
                it.isNotBlank()
            }?.let { storagePath ->
                File(storagePath).exists()
            } ?: false


    val fileExists: Boolean
        get() = id != null && id != -1L


    val isHidden: Boolean
        get() = fileName.startsWith(".")

    val isSharedWithMe
        get() = permissions != null && permissions.contains(PERMISSION_SHARED_WITH_ME)

    val isAvailableOffline
        get() = availableOfflineStatus?.isOneOf(AVAILABLE_OFFLINE, AVAILABLE_OFFLINE_PARENT) ?: false

    val localModificationTimestamp: Long
        get() =
            storagePath?.takeIf {
                it.isNotBlank()
            }?.let { storagePath ->
                File(storagePath).lastModified()
            } ?: 0

    fun copyLocalPropertiesFrom(sourceFile: OCFile) {
        parentId = sourceFile.parentId
        id = sourceFile.id
        lastSyncDateForData = sourceFile.lastSyncDateForData
        modifiedAtLastSyncForData = sourceFile.modifiedAtLastSyncForData
        storagePath = sourceFile.storagePath
        treeEtag = sourceFile.treeEtag
        etagInConflict = sourceFile.etagInConflict
        availableOfflineStatus = sourceFile.availableOfflineStatus
        lastUsage = sourceFile.lastUsage
    }


    private fun isOfType(type: String): Boolean =
        mimeType.startsWith(type) || getMimeTypeFromName()?.startsWith(type) ?: false

    fun getMimeTypeFromName(): String? {
        val extension = remotePath.substringAfterLast('.').lowercase(Locale.ROOT)
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }

    companion object {
        const val PATH_SEPARATOR = '/'
        const val ROOT_PATH: String = "/"
        const val ROOT_PARENT_ID: Long = 0
    }
}
