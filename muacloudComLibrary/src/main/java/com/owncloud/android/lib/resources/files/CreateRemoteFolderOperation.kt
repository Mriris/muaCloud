package com.owncloud.android.lib.resources.files

import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.http.HttpConstants
import com.owncloud.android.lib.common.http.methods.webdav.MkColMethod
import com.owncloud.android.lib.common.network.WebdavUtils
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.operations.RemoteOperationResult.ResultCode
import timber.log.Timber
import java.net.URL
import java.util.concurrent.TimeUnit


class CreateRemoteFolderOperation(
    val remotePath: String,
    private val createFullPath: Boolean,
    private val isChunksFolder: Boolean = false,
    val spaceWebDavUrl: String? = null,
) : RemoteOperation<Unit>() {

    override fun run(client: OwnCloudClient): RemoteOperationResult<Unit> {

        var result = createFolder(client)
        if (!result.isSuccess && createFullPath && result.code == ResultCode.CONFLICT) {
            result = createParentFolder(FileUtils.getParentPath(remotePath), client)

            if (result.isSuccess) {

                result = createFolder(client)
            }
        }
        return result
    }

    private fun createFolder(client: OwnCloudClient): RemoteOperationResult<Unit> {
        var result: RemoteOperationResult<Unit>
        try {
            val webDavUri = if (isChunksFolder) {
                client.uploadsWebDavUri.toString()
            } else {
                spaceWebDavUrl ?: client.userFilesWebDavUri.toString()
            }

            val mkCol = MkColMethod(
                URL(webDavUri + WebdavUtils.encodePath(remotePath))
            ).apply {
                setReadTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                setConnectionTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
            }

            val status = client.executeHttpMethod(mkCol)
            result =
                if (status == HttpConstants.HTTP_CREATED) {
                    RemoteOperationResult(ResultCode.OK)
                } else {
                    RemoteOperationResult(mkCol)
                }

            Timber.d("Create directory $remotePath - HTTP status code: $status")
            client.exhaustResponse(mkCol.getResponseBodyAsStream())

        } catch (e: Exception) {
            result = RemoteOperationResult(e)
            Timber.e(e, "Create directory $remotePath: ${result.logMessage}")
        }
        return result
    }

    private fun createParentFolder(parentPath: String, client: OwnCloudClient): RemoteOperationResult<Unit> {
        val operation: RemoteOperation<Unit> = CreateRemoteFolderOperation(parentPath, createFullPath)
        return operation.execute(client)
    }

    companion object {
        private const val READ_TIMEOUT: Long = 30_000
        private const val CONNECTION_TIMEOUT: Long = 5_000
    }
}
