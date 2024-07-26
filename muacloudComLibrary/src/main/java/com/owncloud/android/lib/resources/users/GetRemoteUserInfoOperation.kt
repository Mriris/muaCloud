package com.owncloud.android.lib.resources.users

import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.http.HttpConstants
import com.owncloud.android.lib.common.http.methods.nonwebdav.GetMethod
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.operations.RemoteOperationResult.ResultCode
import com.owncloud.android.lib.resources.CommonOcsResponse
import com.owncloud.android.lib.resources.users.responses.UserInfoResponse
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import timber.log.Timber
import java.lang.reflect.Type
import java.net.URL


class GetRemoteUserInfoOperation : RemoteOperation<RemoteUserInfo>() {
    override fun run(client: OwnCloudClient): RemoteOperationResult<RemoteUserInfo> {
        var result: RemoteOperationResult<RemoteUserInfo>

        try {
            val getMethod = GetMethod(URL(client.baseUri.toString() + OCS_ROUTE))
            val status = client.executeHttpMethod(getMethod)
            val response = getMethod.getResponseBodyAsString()
            if (status == HttpConstants.HTTP_OK) {
                Timber.d("Successful response $response")

                val moshi: Moshi = Moshi.Builder().build()
                val type: Type = Types.newParameterizedType(CommonOcsResponse::class.java, UserInfoResponse::class.java)
                val adapter: JsonAdapter<CommonOcsResponse<UserInfoResponse>> = moshi.adapter(type)
                val commonResponse: CommonOcsResponse<UserInfoResponse>? = adapter.fromJson(response)

                result = RemoteOperationResult(ResultCode.OK)
                result.data = commonResponse?.ocs?.data?.toRemoteUserInfo()

                Timber.d("Get User Info completed and parsed to ${result.data}")

            } else {
                result = RemoteOperationResult(getMethod)
                Timber.e("Failed response while getting user information status code: $status, response: $response")
            }
        } catch (e: Exception) {
            result = RemoteOperationResult(e)
            Timber.e(e, "Exception while getting OC user information")
        }
        return result
    }

    companion object {

        private const val OCS_ROUTE = "/ocs/v2.php/cloud/user?format=json"
    }
}
