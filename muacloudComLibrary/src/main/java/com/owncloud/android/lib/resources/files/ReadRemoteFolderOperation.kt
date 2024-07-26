package com.owncloud.android.lib.resources.files

import at.bitfire.dav4jvm.PropertyRegistry
import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.accounts.AccountUtils
import com.owncloud.android.lib.common.http.HttpConstants.HTTP_MULTI_STATUS
import com.owncloud.android.lib.common.http.HttpConstants.HTTP_OK
import com.owncloud.android.lib.common.http.methods.webdav.DavConstants
import com.owncloud.android.lib.common.http.methods.webdav.DavUtils
import com.owncloud.android.lib.common.http.methods.webdav.PropfindMethod
import com.owncloud.android.lib.common.http.methods.webdav.properties.OCShareTypes
import com.owncloud.android.lib.common.network.WebdavUtils
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.operations.RemoteOperationResult.ResultCode
import com.owncloud.android.lib.common.utils.isOneOf
import timber.log.Timber
import java.net.URL


class ReadRemoteFolderOperation(
    val remotePath: String,
    val spaceWebDavUrl: String? = null,
) : RemoteOperation<ArrayList<RemoteFile>>() {


    override fun run(client: OwnCloudClient): RemoteOperationResult<ArrayList<RemoteFile>> {
        try {
            PropertyRegistry.register(OCShareTypes.Factory())

            val propfindMethod = PropfindMethod(
                getFinalWebDavUrl(),
                DavConstants.DEPTH_1,
                DavUtils.allPropSet
            )

            val status = client.executeHttpMethod(propfindMethod)

            if (isSuccess(status)) {
                val mFolderAndFiles = ArrayList<RemoteFile>()

                val remoteFolder = RemoteFile.getRemoteFileFromDav(
                    davResource = propfindMethod.root!!,
                    userId = AccountUtils.getUserId(mAccount, mContext),
                    userName = mAccount.name,
                    spaceWebDavUrl = spaceWebDavUrl,
                )
                mFolderAndFiles.add(remoteFolder)

                propfindMethod.members.forEach { resource ->
                    val remoteFile = RemoteFile.getRemoteFileFromDav(
                        davResource = resource,
                        userId = AccountUtils.getUserId(mAccount, mContext),
                        userName = mAccount.name,
                        spaceWebDavUrl = spaceWebDavUrl,
                    )
                    mFolderAndFiles.add(remoteFile)
                }

                return RemoteOperationResult<ArrayList<RemoteFile>>(ResultCode.OK).apply {
                    data = mFolderAndFiles
                    Timber.i("Synchronized $remotePath with ${mFolderAndFiles.size} files. - HTTP status code: $status")
                }
            } else { // synchronization failed
                return RemoteOperationResult<ArrayList<RemoteFile>>(propfindMethod).also {
                    Timber.w("Synchronized $remotePath ${it.logMessage}")
                }
            }
        } catch (e: Exception) {
            return RemoteOperationResult<ArrayList<RemoteFile>>(e).also {
                Timber.e(it.exception, "Synchronized $remotePath")
            }
        }
    }

    private fun getFinalWebDavUrl(): URL {
        val baseWebDavUrl = spaceWebDavUrl ?: client.userFilesWebDavUri.toString()

        return URL(baseWebDavUrl + WebdavUtils.encodePath(remotePath))
    }

    private fun isSuccess(status: Int): Boolean = status.isOneOf(HTTP_OK, HTTP_MULTI_STATUS)
}
