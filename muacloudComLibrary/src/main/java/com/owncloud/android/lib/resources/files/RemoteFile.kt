
package com.owncloud.android.lib.resources.files

import android.net.Uri
import android.os.Parcelable
import androidx.annotation.VisibleForTesting
import at.bitfire.dav4jvm.PropStat
import at.bitfire.dav4jvm.Property
import at.bitfire.dav4jvm.Response
import at.bitfire.dav4jvm.property.CreationDate
import at.bitfire.dav4jvm.property.GetContentLength
import at.bitfire.dav4jvm.property.GetContentType
import at.bitfire.dav4jvm.property.GetETag
import at.bitfire.dav4jvm.property.GetLastModified
import at.bitfire.dav4jvm.property.OCId
import at.bitfire.dav4jvm.property.OCPermissions
import at.bitfire.dav4jvm.property.OCPrivatelink
import at.bitfire.dav4jvm.property.OCSize
import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.http.HttpConstants
import com.owncloud.android.lib.common.http.methods.webdav.properties.OCShareTypes
import com.owncloud.android.lib.common.utils.isOneOf
import com.owncloud.android.lib.resources.shares.ShareType
import com.owncloud.android.lib.resources.shares.ShareType.Companion.fromValue
import kotlinx.parcelize.Parcelize
import okhttp3.HttpUrl
import timber.log.Timber
import java.io.File


@Parcelize
data class RemoteFile(
    var remotePath: String,
    var mimeType: String = "DIR",
    var length: Long = 0,
    var creationTimestamp: Long = 0,
    var modifiedTimestamp: Long = 0,
    var etag: String? = null,
    var permissions: String? = null,
    var remoteId: String? = null,
    var size: Long = 0,
    var privateLink: String? = null,
    var owner: String,
    var sharedByLink: Boolean = false,
    var sharedWithSharee: Boolean = false,
) : Parcelable {

    init {
        require(
            !(remotePath.isEmpty() || !remotePath.startsWith(File.separator))
        ) { "Trying to create a OCFile with a non valid remote path: $remotePath" }
    }


    val isFolder
        get() = mimeType.isOneOf(MIME_DIR, MIME_DIR_UNIX)

    companion object {

        const val MIME_DIR = "DIR"
        const val MIME_DIR_UNIX = "httpd/unix-directory"

        fun getRemoteFileFromDav(
            davResource: Response,
            userId: String,
            userName: String,
            spaceWebDavUrl: String? = null
        ): RemoteFile {
            val remotePath = getRemotePathFromUrl(davResource.href, userId, spaceWebDavUrl)
            val remoteFile = RemoteFile(remotePath = remotePath, owner = userName)
            val properties = getPropertiesEvenIfPostProcessing(davResource)

            for (property in properties) {
                when (property) {
                    is CreationDate -> {
                        remoteFile.creationTimestamp = property.creationDate.toLong()
                    }
                    is GetContentLength -> {
                        remoteFile.length = property.contentLength
                    }
                    is GetContentType -> {
                        property.type?.let { remoteFile.mimeType = it }
                    }
                    is GetLastModified -> {
                        remoteFile.modifiedTimestamp = property.lastModified
                    }
                    is GetETag -> {
                        remoteFile.etag = property.eTag
                    }
                    is OCPermissions -> {
                        remoteFile.permissions = property.permission
                    }
                    is OCId -> {
                        remoteFile.remoteId = property.id
                    }
                    is OCSize -> {
                        remoteFile.size = property.size
                    }
                    is OCPrivatelink -> {
                        remoteFile.privateLink = property.link
                    }
                    is OCShareTypes -> {
                        val list = property.shareTypes
                        for (i in list.indices) {
                            val shareType = fromValue(list[i].toInt())
                            if (shareType == null) {
                                Timber.d("Illegal share type value: " + list[i])
                                continue
                            }
                            if (shareType == ShareType.PUBLIC_LINK) {
                                remoteFile.sharedByLink = true
                            } else if (shareType == ShareType.USER || shareType == ShareType.FEDERATED || shareType == ShareType.GROUP) {
                                remoteFile.sharedWithSharee = true
                            }
                        }
                    }
                }
            }
            return remoteFile
        }


        @VisibleForTesting
        fun getRemotePathFromUrl(
            url: HttpUrl,
            userId: String,
            spaceWebDavUrl: String? = null,
        ): String {
            val davFilesPath = spaceWebDavUrl ?: (OwnCloudClient.WEBDAV_FILES_PATH_4_0 + userId)
            val absoluteDavPath = if (spaceWebDavUrl != null) Uri.decode(url.toString()) else Uri.decode(url.encodedPath)
            val pathToOc = absoluteDavPath.split(davFilesPath).first()
            return absoluteDavPath.replace(pathToOc + davFilesPath, "")
        }

        private fun getPropertiesEvenIfPostProcessing(response: Response): List<Property> {
            return if (response.isSuccess())
                response.propstat.filter { propStat -> propStat.isSuccessOrPostProcessing() }.map { it.properties }.flatten()
            else
                emptyList()
        }

        private fun PropStat.isSuccessOrPostProcessing() = (status.code / 100 == 2 || status.code == HttpConstants.HTTP_TOO_EARLY)
    }
}
