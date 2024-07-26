package com.owncloud.android.lib.resources.spaces

import android.net.Uri
import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.http.HttpConstants
import com.owncloud.android.lib.common.http.methods.nonwebdav.GetMethod
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.resources.spaces.responses.SpaceResponse
import com.owncloud.android.lib.resources.spaces.responses.SpacesResponseWrapper
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import timber.log.Timber
import java.net.URL

class GetRemoteSpacesOperation : RemoteOperation<List<SpaceResponse>>() {
    override fun run(client: OwnCloudClient): RemoteOperationResult<List<SpaceResponse>> {
        val requestUri = buildRequestUri(client.baseUri)

        val getMethod = GetMethod(URL(requestUri.toString()))

        return try {
            val status = client.executeHttpMethod(getMethod)
            val response = getMethod.getResponseBodyAsString()

            if (isSuccess(status)) {
                onRequestSuccessful(response)
            } else {
                onResultUnsuccessful(getMethod, response, status)
            }
        } catch (e: Exception) {
            Timber.e(e, "Exception while getting remote spaces")
            RemoteOperationResult(e)
        }
    }

    private fun buildRequestUri(baseUri: Uri) =
        baseUri.buildUpon()
            .appendEncodedPath(GRAPH_API_PATH)
            .appendEncodedPath(ENDPOINT_SPACES_LIST)
            .build()

    private fun parseResponse(response: String): List<SpaceResponse> {
        val moshi = Moshi.Builder().build()
        val adapter: JsonAdapter<SpacesResponseWrapper> = moshi.adapter(SpacesResponseWrapper::class.java)
        return adapter.fromJson(response)?.value ?: listOf()
    }

    private fun onResultUnsuccessful(
        method: GetMethod,
        response: String?,
        status: Int
    ): RemoteOperationResult<List<SpaceResponse>> {
        Timber.e("Failed response while getting spaces for user")
        if (response != null) {
            Timber.e("*** status code: $status; response message: $response")
        } else {
            Timber.e("*** status code: $status")
        }
        return RemoteOperationResult(method)
    }

    private fun onRequestSuccessful(response: String?): RemoteOperationResult<List<SpaceResponse>> {
        val result = RemoteOperationResult<List<SpaceResponse>>(RemoteOperationResult.ResultCode.OK)
        Timber.d("Successful response: $response")
        result.data = response?.let { parseResponse(it) } ?: listOf()
        Timber.d("*** Fetch of spaces completed and parsed to ${result.data}")
        return result
    }

    private fun isSuccess(status: Int) = status == HttpConstants.HTTP_OK

    companion object {
        private const val GRAPH_API_PATH = "graph/v1.0"
        private const val ENDPOINT_SPACES_LIST = "me/drives"
    }
}
