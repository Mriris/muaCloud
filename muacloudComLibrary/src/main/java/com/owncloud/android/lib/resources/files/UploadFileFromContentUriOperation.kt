
package com.owncloud.android.lib.resources.files

import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.http.HttpConstants
import com.owncloud.android.lib.common.http.methods.webdav.PutMethod
import com.owncloud.android.lib.common.network.ContentUriRequestBody
import com.owncloud.android.lib.common.network.WebdavUtils
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.isOneOf
import timber.log.Timber
import java.net.URL

class UploadFileFromContentUriOperation(
    private val uploadPath: String,
    private val lastModified: String,
    private val requestBody: ContentUriRequestBody
) : RemoteOperation<Unit>() {

    override fun run(client: OwnCloudClient): RemoteOperationResult<Unit> {
        val putMethod = PutMethod(URL(client.userFilesWebDavUri.toString() + WebdavUtils.encodePath(uploadPath)), requestBody).apply {
            retryOnConnectionFailure = false
            addRequestHeader(HttpConstants.OC_TOTAL_LENGTH_HEADER, requestBody.contentLength().toString())
            addRequestHeader(HttpConstants.OC_X_OC_MTIME_HEADER, lastModified)
        }

        return try {
            val status = client.executeHttpMethod(putMethod)
            if (isSuccess(status)) {
                RemoteOperationResult<Unit>(RemoteOperationResult.ResultCode.OK).apply { data = Unit }
            } else {
                RemoteOperationResult<Unit>(putMethod)
            }
        } catch (e: Exception) {
            val result = RemoteOperationResult<Unit>(e)
            Timber.e(e, "Upload from content uri failed : ${result.logMessage}")
            result
        }
    }

    fun isSuccess(status: Int): Boolean {
        return status.isOneOf(HttpConstants.HTTP_OK, HttpConstants.HTTP_CREATED, HttpConstants.HTTP_NO_CONTENT)
    }
}
