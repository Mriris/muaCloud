
package com.owncloud.android.lib.resources.files

import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.http.HttpConstants.HTTP_NO_CONTENT
import com.owncloud.android.lib.common.http.HttpConstants.HTTP_OK
import com.owncloud.android.lib.common.http.methods.nonwebdav.DeleteMethod
import com.owncloud.android.lib.common.network.WebdavUtils
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.operations.RemoteOperationResult.ResultCode
import com.owncloud.android.lib.common.utils.isOneOf
import timber.log.Timber
import java.net.URL


open class RemoveRemoteFileOperation(
    private val remotePath: String,
    val spaceWebDavUrl: String? = null,
) : RemoteOperation<Unit>() {

    override fun run(client: OwnCloudClient): RemoteOperationResult<Unit> {
        var result: RemoteOperationResult<Unit>
        try {
            val srcWebDavUri = getSrcWebDavUriForClient(client)
            val deleteMethod = DeleteMethod(
                URL(srcWebDavUri + WebdavUtils.encodePath(remotePath))
            )
            val status = client.executeHttpMethod(deleteMethod)

            result = if (isSuccess(status)) {
                RemoteOperationResult<Unit>(ResultCode.OK)
            } else {
                RemoteOperationResult<Unit>(deleteMethod)
            }
            Timber.i("Remove $remotePath - HTTP status code: $status")
        } catch (e: Exception) {
            result = RemoteOperationResult<Unit>(e)
            Timber.e(e, "Remove $remotePath: ${result.logMessage}")
        }
        return result
    }


    open fun getSrcWebDavUriForClient(client: OwnCloudClient): String = spaceWebDavUrl ?: client.userFilesWebDavUri.toString()

    private fun isSuccess(status: Int) = status.isOneOf(HTTP_OK, HTTP_NO_CONTENT)
}
