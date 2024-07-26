
package com.owncloud.android.lib.resources.oauth

import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.http.HttpConstants
import com.owncloud.android.lib.common.http.methods.nonwebdav.PostMethod
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.resources.oauth.params.ClientRegistrationParams
import com.owncloud.android.lib.resources.oauth.responses.ClientRegistrationResponse
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import timber.log.Timber
import java.net.URL

class RegisterClientRemoteOperation(
    private val clientRegistrationParams: ClientRegistrationParams
) : RemoteOperation<ClientRegistrationResponse>() {

    override fun run(client: OwnCloudClient): RemoteOperationResult<ClientRegistrationResponse> {
        try {
            val requestBody = clientRegistrationParams.toRequestBody()

            val postMethod = PostMethod(
                url = URL(clientRegistrationParams.registrationEndpoint),
                postRequestBody = requestBody
            )

            val status = client.executeHttpMethod(postMethod)

            val responseBody = postMethod.getResponseBodyAsString()

            if (status == HttpConstants.HTTP_CREATED && responseBody != null) {
                Timber.d("Successful response $responseBody")

                val moshi: Moshi = Moshi.Builder().build()
                val jsonAdapter: JsonAdapter<ClientRegistrationResponse> =
                    moshi.adapter(ClientRegistrationResponse::class.java)
                val clientRegistrationResponse: ClientRegistrationResponse? = jsonAdapter.fromJson(responseBody)
                Timber.d("Client registered and parsed to $clientRegistrationResponse")

                return RemoteOperationResult<ClientRegistrationResponse>(RemoteOperationResult.ResultCode.OK).apply {
                    data = clientRegistrationResponse
                }

            } else {
                Timber.e("Failed response while registering a new client. Status code: $status; response message: $responseBody")
                return RemoteOperationResult<ClientRegistrationResponse>(postMethod)
            }

        } catch (e: Exception) {
            Timber.e(e, "Exception while registering a new client.")
            return RemoteOperationResult<ClientRegistrationResponse>(e)

        }

    }
}
