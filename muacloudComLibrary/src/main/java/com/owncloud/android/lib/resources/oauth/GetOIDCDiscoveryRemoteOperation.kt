package com.owncloud.android.lib.resources.oauth

import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.http.HttpConstants
import com.owncloud.android.lib.common.http.methods.nonwebdav.GetMethod
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.resources.oauth.responses.OIDCDiscoveryResponse
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import timber.log.Timber
import java.net.URL


class GetOIDCDiscoveryRemoteOperation : RemoteOperation<OIDCDiscoveryResponse>() {

    override fun run(client: OwnCloudClient): RemoteOperationResult<OIDCDiscoveryResponse> {
        try {
            val uriBuilder = client.baseUri.buildUpon().apply {
                appendPath(WELL_KNOWN_PATH)    // avoid starting "/" in this method
                appendPath(OPENID_CONFIGURATION_RESOURCE)
            }.build()

            val getMethod = GetMethod(URL(uriBuilder.toString())).apply {
                addRequestHeader(OCS_API_HEADER, OCS_API_HEADER_VALUE)
            }

            getMethod.followRedirects = true
            val status = client.executeHttpMethod(getMethod)

            val responseBody = getMethod.getResponseBodyAsString()

            if (status == HttpConstants.HTTP_OK && responseBody != null) {
                Timber.d("Successful response $responseBody")

                val moshi: Moshi = Moshi.Builder().build()
                val jsonAdapter: JsonAdapter<OIDCDiscoveryResponse> = moshi.adapter(OIDCDiscoveryResponse::class.java)
                val oidcDiscoveryResponse: OIDCDiscoveryResponse? = jsonAdapter.fromJson(responseBody)
                Timber.d("Get OIDC Discovery completed and parsed to [$oidcDiscoveryResponse]")

                return RemoteOperationResult<OIDCDiscoveryResponse>(RemoteOperationResult.ResultCode.OK).apply {
                    data = oidcDiscoveryResponse
                }

            } else {
                Timber.e("Failed response while getting OIDC server discovery from the server status code: $status; response message: $responseBody")

                return RemoteOperationResult<OIDCDiscoveryResponse>(getMethod)
            }

        } catch (e: Exception) {
            Timber.e(e, "Exception while getting OIDC server discovery")

            return RemoteOperationResult<OIDCDiscoveryResponse>(e)
        }
    }

    companion object {
        private const val WELL_KNOWN_PATH = ".well-known"
        private const val OPENID_CONFIGURATION_RESOURCE = "openid-configuration"

    }
}
