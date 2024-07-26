
package com.owncloud.android.lib.resources.status

import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.http.HttpConstants
import com.owncloud.android.lib.common.http.methods.nonwebdav.GetMethod
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.resources.status.HttpScheme.HTTPS_SCHEME
import org.json.JSONObject
import java.net.URL
import java.util.concurrent.TimeUnit

internal class StatusRequester {


    fun isRedirectedToNonSecureConnection(
        redirectedToNonSecureLocationBefore: Boolean,
        baseUrl: String,
        redirectedUrl: String
    ) = redirectedToNonSecureLocationBefore ||
            (baseUrl.startsWith(HTTPS_SCHEME) &&
                    !redirectedUrl.startsWith(HTTPS_SCHEME))

    fun updateLocationWithRedirectPath(oldLocation: String, redirectedLocation: String): String {
                if (redirectedLocation.endsWith('/')) {
            return redirectedLocation.trimEnd('/') + OwnCloudClient.STATUS_PATH
        }

        if (!redirectedLocation.startsWith("/"))
            return redirectedLocation
        val oldLocationURL = URL(oldLocation)
        return URL(oldLocationURL.protocol, oldLocationURL.host, oldLocationURL.port, redirectedLocation).toString()
    }

    private fun getGetMethod(url: String): GetMethod {
        return GetMethod(URL(url)).apply {
            setReadTimeout(TRY_CONNECTION_TIMEOUT, TimeUnit.SECONDS)
            setConnectionTimeout(TRY_CONNECTION_TIMEOUT, TimeUnit.SECONDS)
        }
    }

    data class RequestResult(
        val getMethod: GetMethod,
        val status: Int,
        val lastLocation: String
    )

    fun request(baseLocation: String, client: OwnCloudClient): RequestResult {
        val currentLocation = baseLocation + OwnCloudClient.STATUS_PATH
        var status: Int
        val getMethod = getGetMethod(currentLocation)

        getMethod.followPermanentRedirects = true
        status = client.executeHttpMethod(getMethod)

        return RequestResult(getMethod, status, getMethod.getFinalUrl().toString())
    }

    private fun Int.isSuccess() = this == HttpConstants.HTTP_OK

    fun handleRequestResult(
        requestResult: RequestResult,
        baseUrl: String
    ): RemoteOperationResult<RemoteServerInfo> {
        if (!requestResult.status.isSuccess())
            return RemoteOperationResult(requestResult.getMethod)

        val respJSON = JSONObject(requestResult.getMethod.getResponseBodyAsString())
        if (!respJSON.getBoolean(NODE_INSTALLED))
            return RemoteOperationResult(RemoteOperationResult.ResultCode.INSTANCE_NOT_CONFIGURED)

        val ocVersion = OwnCloudVersion(respJSON.getString(NODE_VERSION))


        val result: RemoteOperationResult<RemoteServerInfo> =
            if (baseUrl.startsWith(HTTPS_SCHEME)) RemoteOperationResult(RemoteOperationResult.ResultCode.OK_SSL)
            else RemoteOperationResult(RemoteOperationResult.ResultCode.OK_NO_SSL)
        val finalUrl = URL(requestResult.lastLocation)
        val finalBaseUrl = URL(
            finalUrl.protocol,
            finalUrl.host,
            finalUrl.port,
            finalUrl.file.dropLastWhile { it != '/' }.trimEnd('/')
        )

        result.data = RemoteServerInfo(
            ownCloudVersion = ocVersion,
            baseUrl = finalBaseUrl.toString(),
            isSecureConnection = finalBaseUrl.protocol.startsWith(HTTPS_SCHEME)
        )
        return result
    }

    companion object {

        private const val TRY_CONNECTION_TIMEOUT = 5_000L
        private const val NODE_INSTALLED = "installed"
        private const val NODE_VERSION = "version"
    }
}
