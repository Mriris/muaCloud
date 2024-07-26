package com.owncloud.android.lib.resources.appregistry

import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.http.HttpConstants
import com.owncloud.android.lib.common.http.methods.nonwebdav.GetMethod
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.operations.RemoteOperationResult.ResultCode.OK
import com.owncloud.android.lib.resources.appregistry.responses.AppRegistryResponse
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import timber.log.Timber
import java.net.URL

class GetRemoteAppRegistryOperation(private val appUrl: String?) : RemoteOperation<AppRegistryResponse>() {

    override fun run(client: OwnCloudClient): RemoteOperationResult<AppRegistryResponse> {
        var result: RemoteOperationResult<AppRegistryResponse>

        try {
            val urlFormatted = removeSubfolder(client.baseUri.toString()) + appUrl
            val getMethod = GetMethod(URL(urlFormatted))

            val status = client.executeHttpMethod(getMethod)

            val response = getMethod.getResponseBodyAsString()

            if (status == HttpConstants.HTTP_OK) {
                Timber.d("Successful response $response")

                val moshi: Moshi = Moshi.Builder().build()
                val adapter: JsonAdapter<AppRegistryResponse> = moshi.adapter(AppRegistryResponse::class.java)
                val appRegistryResponse: AppRegistryResponse = response.let { adapter.fromJson(it) } ?: AppRegistryResponse(value = emptyList())

                result = RemoteOperationResult(OK)
                result.data = appRegistryResponse

                Timber.d("Get AppRegistry completed and parsed to ${result.data}")
            } else {
                result = RemoteOperationResult(getMethod)
                Timber.e("Failed response while getting app registry from the server status code: $status; response message: $response")
            }

        } catch (e: Exception) {
            result = RemoteOperationResult(e)
            Timber.e(e, "Exception while getting app registry")
        }

        return result
    }

    private fun removeSubfolder(url: String): String {
        val doubleSlashIndex = url.indexOf("//")
        val nextSlashIndex = url.indexOf('/', doubleSlashIndex + 2)
        return if (nextSlashIndex >= 0) {
            val result = url.substring(0, nextSlashIndex)
            if (result.endsWith("/")) result else "$result/"
        } else {
            if (url.endsWith("/")) url else "$url/"
        }
    }
}
