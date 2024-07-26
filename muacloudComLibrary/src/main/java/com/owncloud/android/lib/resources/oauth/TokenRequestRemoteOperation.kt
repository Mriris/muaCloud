package com.owncloud.android.lib.resources.oauth

import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.http.HttpConstants.AUTHORIZATION_HEADER
import com.owncloud.android.lib.common.http.HttpConstants.HTTP_OK
import com.owncloud.android.lib.common.http.methods.nonwebdav.PostMethod
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.resources.oauth.params.TokenRequestParams
import com.owncloud.android.lib.resources.oauth.responses.TokenResponse
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import timber.log.Timber
import java.net.URL


class TokenRequestRemoteOperation(
    private val tokenRequestParams: TokenRequestParams
) : RemoteOperation<TokenResponse>() {

    override fun run(client: OwnCloudClient): RemoteOperationResult<TokenResponse> {
        try {
            val requestBody = tokenRequestParams.toRequestBody()

            val postMethod = PostMethod(URL(tokenRequestParams.tokenEndpoint), requestBody)

            postMethod.addRequestHeader(AUTHORIZATION_HEADER, tokenRequestParams.clientAuth)

            val status = client.executeHttpMethod(postMethod)

            val responseBody = postMethod.getResponseBodyAsString()

            if (status == HTTP_OK && responseBody != null) {
                Timber.d("Successful response $responseBody")

                val moshi: Moshi = Moshi.Builder().build()
                val jsonAdapter: JsonAdapter<TokenResponse> = moshi.adapter(TokenResponse::class.java)
                val tokenResponse: TokenResponse? = jsonAdapter.fromJson(responseBody)
                Timber.d("Get tokens completed and parsed to $tokenResponse")

                return RemoteOperationResult<TokenResponse>(RemoteOperationResult.ResultCode.OK).apply {
                    data = tokenResponse
                }

            } else {
                Timber.e("Failed response while getting tokens from the server status code: $status; response message: $responseBody")
                return RemoteOperationResult<TokenResponse>(postMethod)
            }

        } catch (e: Exception) {
            Timber.e(e, "Exception while getting tokens")
            return RemoteOperationResult<TokenResponse>(e)

        }
    }
}
