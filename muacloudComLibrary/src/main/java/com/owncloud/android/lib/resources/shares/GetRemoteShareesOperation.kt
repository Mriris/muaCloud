
package com.owncloud.android.lib.resources.shares

import android.net.Uri
import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.http.HttpConstants
import com.owncloud.android.lib.common.http.HttpConstants.PARAM_FORMAT
import com.owncloud.android.lib.common.http.HttpConstants.VALUE_FORMAT
import com.owncloud.android.lib.common.http.methods.nonwebdav.GetMethod
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.operations.RemoteOperationResult.ResultCode.OK
import com.owncloud.android.lib.resources.CommonOcsResponse
import com.owncloud.android.lib.resources.shares.responses.ShareeOcsResponse
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import timber.log.Timber
import java.lang.reflect.Type
import java.net.URL


class GetRemoteShareesOperation

    (private val searchString: String, private val page: Int, private val perPage: Int) :
    RemoteOperation<ShareeOcsResponse>() {

    private fun buildRequestUri(baseUri: Uri) =
        baseUri.buildUpon()
            .appendEncodedPath(OCS_ROUTE)
            .appendQueryParameter(PARAM_FORMAT, VALUE_FORMAT)
            .appendQueryParameter(PARAM_ITEM_TYPE, VALUE_ITEM_TYPE)
            .appendQueryParameter(PARAM_SEARCH, searchString)
            .appendQueryParameter(PARAM_PAGE, page.toString())
            .appendQueryParameter(PARAM_PER_PAGE, perPage.toString())
            .build()

    private fun parseResponse(response: String?): ShareeOcsResponse? {
        val moshi = Moshi.Builder().build()
        val type: Type = Types.newParameterizedType(CommonOcsResponse::class.java, ShareeOcsResponse::class.java)
        val adapter: JsonAdapter<CommonOcsResponse<ShareeOcsResponse>> = moshi.adapter(type)
        return response?.let { adapter.fromJson(it)?.ocs?.data }
    }

    private fun onResultUnsuccessful(
        method: GetMethod,
        response: String?,
        status: Int
    ): RemoteOperationResult<ShareeOcsResponse> {
        Timber.e("Failed response while getting users/groups from the server ")
        if (response != null) {
            Timber.e("*** status code: $status; response message: $response")
        } else {
            Timber.e("*** status code: $status")
        }
        return RemoteOperationResult(method)
    }

    private fun onRequestSuccessful(response: String?): RemoteOperationResult<ShareeOcsResponse> {
        val result = RemoteOperationResult<ShareeOcsResponse>(OK)
        Timber.d("Successful response: $response")
        result.data = parseResponse(response)
        Timber.d("*** Get Users or groups completed ")
        return result
    }

    override fun run(client: OwnCloudClient): RemoteOperationResult<ShareeOcsResponse> {
        val requestUri = buildRequestUri(client.baseUri)

        val getMethod = GetMethod(URL(requestUri.toString()))
        getMethod.addRequestHeader(OCS_API_HEADER, OCS_API_HEADER_VALUE)

        return try {
            val status = client.executeHttpMethod(getMethod)
            val response = getMethod.getResponseBodyAsString()

            if (isSuccess(status)) {
                onRequestSuccessful(response)
            } else {
                onResultUnsuccessful(getMethod, response, status)
            }
        } catch (e: Exception) {
            Timber.e(e, "Exception while getting users/groups")
            RemoteOperationResult(e)
        }
    }

    private fun isSuccess(status: Int) = status == HttpConstants.HTTP_OK

    companion object {

        private const val OCS_ROUTE = "ocs/v2.php/apps/files_sharing/api/v1/sharees"    // from OC 8.2

        private const val PARAM_ITEM_TYPE = "itemType"
        private const val PARAM_SEARCH = "search"
        private const val PARAM_PAGE = "page"                //  default = 1
        private const val PARAM_PER_PAGE = "perPage"         //  default = 200

        private const val VALUE_ITEM_TYPE = "file"         //  to get the server search for users / groups
    }
}
