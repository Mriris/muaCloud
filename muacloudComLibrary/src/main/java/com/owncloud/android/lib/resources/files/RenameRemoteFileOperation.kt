
package com.owncloud.android.lib.resources.files

import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.http.HttpConstants
import com.owncloud.android.lib.common.http.methods.webdav.MoveMethod
import com.owncloud.android.lib.common.network.WebdavUtils
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.operations.RemoteOperationResult.ResultCode
import com.owncloud.android.lib.common.utils.isOneOf
import timber.log.Timber
import java.io.File
import java.net.URL
import java.util.concurrent.TimeUnit


class RenameRemoteFileOperation(
    private val oldName: String,
    private val oldRemotePath: String,
    private val newName: String,
    isFolder: Boolean,
    val spaceWebDavUrl: String? = null,
) : RemoteOperation<Unit>() {

    private var newRemotePath: String

    init {
        var parent = (File(oldRemotePath)).parent ?: throw IllegalArgumentException()
        if (!parent.endsWith(File.separator)) {
            parent = parent.plus(File.separator)
        }
        newRemotePath = parent.plus(newName)
        if (isFolder) {
            newRemotePath.plus(File.separator)
        }
    }

    override fun run(client: OwnCloudClient): RemoteOperationResult<Unit> {
        var result: RemoteOperationResult<Unit>
        try {
            if (newName == oldName) {
                return RemoteOperationResult<Unit>(ResultCode.OK)
            }

            if (targetPathIsUsed(client)) {
                return RemoteOperationResult<Unit>(ResultCode.INVALID_OVERWRITE)
            }

            val moveMethod: MoveMethod = MoveMethod(
                url = URL((spaceWebDavUrl ?: client.userFilesWebDavUri.toString()) + WebdavUtils.encodePath(oldRemotePath)),
                destinationUrl = (spaceWebDavUrl ?: client.userFilesWebDavUri.toString()) + WebdavUtils.encodePath(newRemotePath),
            ).apply {
                setReadTimeout(RENAME_READ_TIMEOUT, TimeUnit.MILLISECONDS)
                setConnectionTimeout(RENAME_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
            }
            val status = client.executeHttpMethod(moveMethod)

            result = if (isSuccess(status)) {
                RemoteOperationResult<Unit>(ResultCode.OK)
            } else {
                RemoteOperationResult<Unit>(moveMethod)
            }

            Timber.i("Rename $oldRemotePath to $newRemotePath - HTTP status code: $status")
            client.exhaustResponse(moveMethod.getResponseBodyAsStream())
            return result
        } catch (exception: Exception) {
            result = RemoteOperationResult<Unit>(exception)
            Timber.e(exception, "Rename $oldRemotePath to $newName: ${result.logMessage}")
            return result
        }
    }


    private fun targetPathIsUsed(client: OwnCloudClient): Boolean {
        val checkPathExistenceRemoteOperation = CheckPathExistenceRemoteOperation(newRemotePath, true)
        val exists = checkPathExistenceRemoteOperation.execute(client)
        return exists.isSuccess
    }

    private fun isSuccess(status: Int) = status.isOneOf(HttpConstants.HTTP_CREATED, HttpConstants.HTTP_NO_CONTENT)

    companion object {
        private const val RENAME_READ_TIMEOUT = 10_000L
        private const val RENAME_CONNECTION_TIMEOUT = 5_000L
    }
}
