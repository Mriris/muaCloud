package com.owncloud.android.lib.resources.files

import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.http.HttpConstants
import com.owncloud.android.lib.common.http.methods.webdav.CopyMethod
import com.owncloud.android.lib.common.network.WebdavUtils
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.operations.RemoteOperationResult.ResultCode
import com.owncloud.android.lib.common.utils.isOneOf
import timber.log.Timber
import java.net.URL
import java.util.concurrent.TimeUnit


class CopyRemoteFileOperation(
    private val sourceRemotePath: String,
    private val targetRemotePath: String,
    private val sourceSpaceWebDavUrl: String? = null,
    private val targetSpaceWebDavUrl: String? = null,
    private val forceOverride: Boolean = false,
) : RemoteOperation<String>() {


    override fun run(client: OwnCloudClient): RemoteOperationResult<String> {
        if (targetRemotePath == sourceRemotePath && sourceSpaceWebDavUrl == targetSpaceWebDavUrl) {

            return RemoteOperationResult(ResultCode.OK)
        }

        var result: RemoteOperationResult<String>
        try {
            val copyMethod = CopyMethod(
                url = URL((sourceSpaceWebDavUrl ?: client.userFilesWebDavUri.toString()) + WebdavUtils.encodePath(sourceRemotePath)),
                destinationUrl = (targetSpaceWebDavUrl ?: client.userFilesWebDavUri.toString()) + WebdavUtils.encodePath(targetRemotePath),
                forceOverride = forceOverride,
            ).apply {
                addRequestHeaders(this)
                setReadTimeout(COPY_READ_TIMEOUT, TimeUnit.SECONDS)
                setConnectionTimeout(COPY_CONNECTION_TIMEOUT, TimeUnit.SECONDS)
            }
            val status = client.executeHttpMethod(copyMethod)
            when {
                isSuccess(status) -> {
                    val fileRemoteId = copyMethod.getResponseHeader(HttpConstants.OC_FILE_REMOTE_ID)
                    result = RemoteOperationResult(ResultCode.OK)
                    result.setData(fileRemoteId)
                }

                isPreconditionFailed(status) -> {
                    result = RemoteOperationResult(ResultCode.INVALID_OVERWRITE)
                    client.exhaustResponse(copyMethod.getResponseBodyAsStream())


                }

                else -> {
                    result = RemoteOperationResult(copyMethod)
                    client.exhaustResponse(copyMethod.getResponseBodyAsStream())
                }
            }
            Timber.i("Copy $sourceRemotePath to $targetRemotePath - HTTP status code: $status")
        } catch (e: Exception) {
            result = RemoteOperationResult(e)
            Timber.e(e, "Copy $sourceRemotePath to $targetRemotePath: ${result.logMessage}")
        }
        return result
    }

    private fun addRequestHeaders(copyMethod: CopyMethod) {

        if (copyMethod.forceOverride) {
            copyMethod.setRequestHeader(OVERWRITE, TRUE)
        }
    }

    private fun isSuccess(status: Int) = status.isOneOf(HttpConstants.HTTP_CREATED, HttpConstants.HTTP_NO_CONTENT)

    private fun isPreconditionFailed(status: Int) = status == HttpConstants.HTTP_PRECONDITION_FAILED

    companion object {
        private const val COPY_READ_TIMEOUT = 10L
        private const val COPY_CONNECTION_TIMEOUT = 6L
        private const val OVERWRITE = "overwrite"
        private const val TRUE = "T"
    }
}
