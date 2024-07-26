
package com.owncloud.android.lib.resources.shares

import android.net.Uri
import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.http.HttpConstants
import com.owncloud.android.lib.common.http.HttpConstants.PARAM_FORMAT
import com.owncloud.android.lib.common.http.HttpConstants.VALUE_FORMAT
import com.owncloud.android.lib.common.http.methods.nonwebdav.GetMethod
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.resources.CommonOcsResponse
import com.owncloud.android.lib.resources.shares.responses.ShareItem
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import timber.log.Timber
import java.lang.reflect.Type
import java.net.URL

class GetRemoteShareOperation(private val remoteId: String) : RemoteOperation<ShareResponse>() {

    private fun buildRequestUri(baseUri: Uri) =
        baseUri.buildUpon()
            .appendEncodedPath(OCS_ROUTE)
            .appendEncodedPath(remoteId)
            .appendQueryParameter(PARAM_FORMAT, VALUE_FORMAT)
            .build()

    private fun parseResponse(response: String): ShareResponse? {
        val moshi = Moshi.Builder().build()
        val listOfShareItemType: Type = Types.newParameterizedType(List::class.java, ShareItem::class.java)
        val commonOcsType: Type = Types.newParameterizedType(CommonOcsResponse::class.java, listOfShareItemType)
        val adapter: JsonAdapter<CommonOcsResponse<List<ShareItem>>> = moshi.adapter(commonOcsType)
        return adapter.fromJson(response)?.ocs?.data?.let { listOfShareItems ->
            ShareResponse(listOfShareItems.map { shareItem ->
                shareItem.toRemoteShare()
            })
        }
    }

    private fun onResultUnsuccessful(
        method: GetMethod,
        response: String?,
        status: Int
    ): RemoteOperationResult<ShareResponse> {
        Timber.e("Failed response while while getting remote shares ")
        if (response != null) {
            Timber.e("*** status code: $status; response message: $response")
        } else {
            Timber.e("*** status code: $status")
        }
        return RemoteOperationResult(method)
    }

    private fun onRequestSuccessful(response: String?): RemoteOperationResult<ShareResponse> {
        val result = RemoteOperationResult<ShareResponse>(RemoteOperationResult.ResultCode.OK)
        Timber.d("Successful response: $response")
        result.data = parseResponse(response!!)
        Timber.d("*** Get Users or groups completed ")
        return result
    }

    override fun run(client: OwnCloudClient): RemoteOperationResult<ShareResponse> {
        val requestUri = buildRequestUri(client.baseUri)

        val getMethod = GetMethod(URL(requestUri.toString())).apply {
            addRequestHeader(OCS_API_HEADER, OCS_API_HEADER_VALUE)
        }

        return try {
            val status = client.executeHttpMethod(getMethod)
            val response = getMethod.getResponseBodyAsString()

            if (!isSuccess(status)) {
                onResultUnsuccessful(getMethod, response, status)
            } else {
                onRequestSuccessful(response)
            }
        } catch (e: Exception) {
            Timber.e(e, "Exception while getting remote shares")
            RemoteOperationResult(e)
        }
    }

    private fun isSuccess(status: Int) = status == HttpConstants.HTTP_OK

    companion object {

        private const val OCS_ROUTE = "ocs/v2.php/apps/files_sharing/api/v1/shares"
    }
}
