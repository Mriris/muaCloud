
package com.owncloud.android.lib.resources.webfinger

import android.net.Uri
import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.http.HttpConstants
import com.owncloud.android.lib.common.http.methods.nonwebdav.GetMethod
import com.owncloud.android.lib.common.http.methods.nonwebdav.HttpMethod
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.resources.webfinger.responses.WebFingerResponse
import com.squareup.moshi.Moshi
import timber.log.Timber
import java.net.URL

class GetInstancesViaWebFingerOperation(
    private val lockupServerDomain: String,
    private val rel: String,
    private val resource: String,
) : RemoteOperation<List<String>>() {

    private fun buildRequestUri() =
        Uri.parse(lockupServerDomain).buildUpon()
            .path(ENDPOINT_WEBFINGER_PATH)
            .appendQueryParameter("rel", rel)
            .appendQueryParameter("resource", resource)
            .build()

    private fun isSuccess(status: Int): Boolean = status == HttpConstants.HTTP_OK

    private fun parseResponse(response: String): WebFingerResponse {
        val moshi = Moshi.Builder().build()
        val adapter = moshi.adapter(WebFingerResponse::class.java)
        return adapter.fromJson(response)!!
    }

    private fun onResultUnsuccessful(
        method: HttpMethod,
        response: String?,
        status: Int
    ): RemoteOperationResult<List<String>> {
        Timber.e("Failed requesting WebFinger info")
        if (response != null) {
            Timber.e("*** status code: $status; response message: $response")
        } else {
            Timber.e("*** status code: $status")
        }
        return RemoteOperationResult<List<String>>(method)
    }

    private fun onRequestSuccessful(rawResponse: String): RemoteOperationResult<List<String>> {
        val response = parseResponse(rawResponse)
        Timber.d("Successful WebFinger request: $response")
        val operationResult = RemoteOperationResult<List<String>>(RemoteOperationResult.ResultCode.OK)
        operationResult.data = response.links?.map { it.href } ?: listOf()
        return operationResult
    }

    override fun run(client: OwnCloudClient): RemoteOperationResult<List<String>> {
        val requestUri = buildRequestUri()
        val getMethod = GetMethod(URL(requestUri.toString()))

        getMethod.followRedirects = false

        return try {
            val status = client.executeHttpMethod(getMethod)
            val response = getMethod.getResponseBodyAsString()

            if (isSuccess(status)) {
                onRequestSuccessful(response)
            } else {
                onResultUnsuccessful(getMethod, response, status)
            }
        } catch (e: Exception) {
            Timber.e(e, "Requesting WebFinger info failed")
            RemoteOperationResult<List<String>>(e)
        }
    }

    companion object {
        private const val ENDPOINT_WEBFINGER_PATH = "/.well-known/webfinger"
    }
}
