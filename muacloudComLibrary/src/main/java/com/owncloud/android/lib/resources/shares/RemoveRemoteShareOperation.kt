
package com.owncloud.android.lib.resources.shares

import android.net.Uri
import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.http.HttpConstants
import com.owncloud.android.lib.common.http.HttpConstants.PARAM_FORMAT
import com.owncloud.android.lib.common.http.HttpConstants.VALUE_FORMAT
import com.owncloud.android.lib.common.http.methods.nonwebdav.DeleteMethod
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import timber.log.Timber
import java.net.URL


class RemoveRemoteShareOperation(private val remoteShareId: String) : RemoteOperation<Unit>() {

    private fun buildRequestUri(baseUri: Uri) =
        baseUri.buildUpon()
            .appendEncodedPath(OCS_ROUTE)
            .appendEncodedPath(remoteShareId)
            .appendQueryParameter(PARAM_FORMAT, VALUE_FORMAT)
            .build()

    private fun onResultUnsuccessful(
        method: DeleteMethod,
        response: String?,
        status: Int
    ): RemoteOperationResult<Unit> {
        Timber.e("Failed response while removing share ")
        if (response != null) {
            Timber.e("*** status code: $status; response message: $response")
        } else {
            Timber.e("*** status code: $status")
        }
        return RemoteOperationResult(method)
    }

    private fun onRequestSuccessful(response: String?): RemoteOperationResult<Unit> {
        val result = RemoteOperationResult<Unit>(RemoteOperationResult.ResultCode.OK)
        Timber.d("Successful response: $response")
        Timber.d("*** Unshare link completed ")
        return result
    }

    override fun run(client: OwnCloudClient): RemoteOperationResult<Unit> {
        val requestUri = buildRequestUri(client.baseUri)

        val deleteMethod = DeleteMethod(URL(requestUri.toString())).apply {
            addRequestHeader(OCS_API_HEADER, OCS_API_HEADER_VALUE)
        }

        return try {
            val status = client.executeHttpMethod(deleteMethod)
            val response = deleteMethod.getResponseBodyAsString()

            if (isSuccess(status)) {
                onRequestSuccessful(response)
            } else {
                onResultUnsuccessful(deleteMethod, response, status)
            }
        } catch (e: Exception) {
            Timber.e(e, "Exception while unshare link")
            RemoteOperationResult(e)
        }
    }

    private fun isSuccess(status: Int): Boolean = status == HttpConstants.HTTP_OK

    companion object {

        private const val OCS_ROUTE = "ocs/v2.php/apps/files_sharing/api/v1/shares"
    }
}
