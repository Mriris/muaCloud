package com.owncloud.android.lib.resources.status

import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.http.HttpConstants
import com.owncloud.android.lib.common.http.methods.nonwebdav.GetMethod
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.operations.RemoteOperationResult.ResultCode.OK
import com.owncloud.android.lib.resources.CommonOcsResponse
import com.owncloud.android.lib.resources.status.responses.CapabilityResponse
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import timber.log.Timber
import java.lang.reflect.Type
import java.net.URL


class GetRemoteCapabilitiesOperation : RemoteOperation<RemoteCapability>() {

    override fun run(client: OwnCloudClient): RemoteOperationResult<RemoteCapability> {
        var result: RemoteOperationResult<RemoteCapability>

        try {
            val uriBuilder = client.baseUri.buildUpon().apply {
                appendEncodedPath(OCS_ROUTE)    // avoid starting "/" in this method
                appendQueryParameter(PARAM_FORMAT, VALUE_FORMAT)
            }
            val getMethod = GetMethod(URL(uriBuilder.build().toString())).apply {
                addRequestHeader(OCS_API_HEADER, OCS_API_HEADER_VALUE)
            }
            val status = client.executeHttpMethod(getMethod)

            val response = getMethod.getResponseBodyAsString()

            if (status == HttpConstants.HTTP_OK) {
                Timber.d("Successful response $response")

                val moshi: Moshi = Moshi.Builder().build()
                val type: Type = Types.newParameterizedType(CommonOcsResponse::class.java, CapabilityResponse::class.java)
                val adapter: JsonAdapter<CommonOcsResponse<CapabilityResponse>> = moshi.adapter(type)
                val commonResponse: CommonOcsResponse<CapabilityResponse>? = response.let { adapter.fromJson(it) }

                result = RemoteOperationResult(OK)
                result.data = commonResponse?.ocs?.data?.toRemoteCapability()

                Timber.d("Get Capabilities completed and parsed to ${result.data}")
            } else {
                result = RemoteOperationResult(getMethod)
                Timber.e("Failed response while getting capabilities from the server status code: $status; response message: $response")
            }

        } catch (e: Exception) {
            result = RemoteOperationResult(e)
            Timber.e(e, "Exception while getting capabilities")
        }

        return result
    }

    companion object {

        private const val OCS_ROUTE = "ocs/v2.php/cloud/capabilities"

        private const val PARAM_FORMAT = "format"

        private const val VALUE_FORMAT = "json"
    }
}
