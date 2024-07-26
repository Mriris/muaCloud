package com.owncloud.android.lib.resources.files

import android.net.Uri
import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.http.HttpConstants
import com.owncloud.android.lib.common.http.methods.webdav.MoveMethod
import com.owncloud.android.lib.common.network.WebdavUtils
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.operations.RemoteOperationResult.ResultCode
import com.owncloud.android.lib.common.utils.isOneOf
import timber.log.Timber
import java.net.URL
import java.util.concurrent.TimeUnit


open class MoveRemoteFileOperation(
    private val sourceRemotePath: String,
    private val targetRemotePath: String,
    private val spaceWebDavUrl: String? = null,
    private val forceOverride: Boolean = false,
) : RemoteOperation<Unit>() {


    override fun run(client: OwnCloudClient): RemoteOperationResult<Unit> {
        if (targetRemotePath == sourceRemotePath) {

            return RemoteOperationResult(ResultCode.OK)
        }

        if (targetRemotePath.startsWith(sourceRemotePath)) {
            return RemoteOperationResult(ResultCode.INVALID_MOVE_INTO_DESCENDANT)
        }

        var result: RemoteOperationResult<Unit>
        try {


            val srcWebDavUri = getSrcWebDavUriForClient(client)
            val moveMethod = MoveMethod(
                url = URL((spaceWebDavUrl ?: srcWebDavUri.toString()) + WebdavUtils.encodePath(sourceRemotePath)),
                destinationUrl = (spaceWebDavUrl ?: client.userFilesWebDavUri.toString()) + WebdavUtils.encodePath(targetRemotePath),
                forceOverride = forceOverride,
            ).apply {
                addRequestHeaders(this)
                setReadTimeout(MOVE_READ_TIMEOUT, TimeUnit.SECONDS)
                setConnectionTimeout(MOVE_CONNECTION_TIMEOUT, TimeUnit.SECONDS)
            }

            val status = client.executeHttpMethod(moveMethod)

            when {
                isSuccess(status) -> {
                    result = RemoteOperationResult<Unit>(ResultCode.OK)
                }

                isPreconditionFailed(status) -> {
                    result = RemoteOperationResult<Unit>(ResultCode.INVALID_OVERWRITE)
                    client.exhaustResponse(moveMethod.getResponseBodyAsStream())


                }

                else -> {
                    result = RemoteOperationResult<Unit>(moveMethod)
                    client.exhaustResponse(moveMethod.getResponseBodyAsStream())
                }
            }

            Timber.i("Move $sourceRemotePath to $targetRemotePath - HTTP status code: $status")
        } catch (e: Exception) {
            result = RemoteOperationResult<Unit>(e)
            Timber.e(e, "Move $sourceRemotePath to $targetRemotePath: ${result.logMessage}")

        }
        return result
    }


    open fun getSrcWebDavUriForClient(client: OwnCloudClient): Uri = client.userFilesWebDavUri


    open fun addRequestHeaders(moveMethod: MoveMethod) {

        if (moveMethod.forceOverride) {
            moveMethod.setRequestHeader(OVERWRITE, TRUE)
        }
    }

    private fun isSuccess(status: Int) = status.isOneOf(HttpConstants.HTTP_CREATED, HttpConstants.HTTP_NO_CONTENT)

    private fun isPreconditionFailed(status: Int) = status == HttpConstants.HTTP_PRECONDITION_FAILED

    companion object {
        private const val MOVE_READ_TIMEOUT = 10L
        private const val MOVE_CONNECTION_TIMEOUT = 6L
        private const val OVERWRITE = "overwrite"
        private const val TRUE = "T"
    }
}
