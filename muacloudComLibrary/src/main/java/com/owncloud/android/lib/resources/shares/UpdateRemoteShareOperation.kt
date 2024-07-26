
package com.owncloud.android.lib.resources.shares

import android.net.Uri
import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.http.HttpConstants
import com.owncloud.android.lib.common.http.HttpConstants.PARAM_FORMAT
import com.owncloud.android.lib.common.http.HttpConstants.VALUE_FORMAT
import com.owncloud.android.lib.common.http.methods.nonwebdav.PutMethod
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.resources.CommonOcsResponse
import com.owncloud.android.lib.resources.shares.RemoteShare.Companion.DEFAULT_PERMISSION
import com.owncloud.android.lib.resources.shares.responses.ShareItem
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import okhttp3.FormBody
import timber.log.Timber
import java.lang.reflect.Type
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class UpdateRemoteShareOperation

    (

    private val remoteId: String

) : RemoteOperation<ShareResponse>() {

    var name: String? = null


    var password: String? = null


    var expirationDateInMillis: Long = INITIAL_EXPIRATION_DATE_IN_MILLIS


    var permissions: Int = DEFAULT_PERMISSION

    var retrieveShareDetails = false // To retrieve more info about the just updated share

    private fun buildRequestUri(baseUri: Uri) =
        baseUri.buildUpon()
            .appendEncodedPath(OCS_ROUTE)
            .appendEncodedPath(remoteId)
            .appendQueryParameter(PARAM_FORMAT, VALUE_FORMAT)
            .build()

    private fun parseResponse(response: String): ShareResponse {
        val moshi = Moshi.Builder().build()
        val commonOcsType: Type = Types.newParameterizedType(CommonOcsResponse::class.java, ShareItem::class.java)
        val adapter: JsonAdapter<CommonOcsResponse<ShareItem>> = moshi.adapter(commonOcsType)
        val remoteShare = adapter.fromJson(response)?.ocs?.data?.toRemoteShare()
        return ShareResponse(remoteShare?.let { listOf(it) } ?: listOf())
    }

    private fun onResultUnsuccessful(
        method: PutMethod,
        response: String?,
        status: Int
    ): RemoteOperationResult<ShareResponse> {
        Timber.e("Failed response while while updating remote shares ")
        if (response != null) {
            Timber.e("*** status code: $status; response message: $response")
        } else {
            Timber.e("*** status code: $status")
        }
        return RemoteOperationResult(method)
    }

    private fun onRequestSuccessful(response: String?): RemoteOperationResult<ShareResponse> {
        val result = RemoteOperationResult<ShareResponse>(RemoteOperationResult.ResultCode.OK)
        Timber.d("Successful response: $response")
        result.data = parseResponse(response!!)
        Timber.d("*** Retrieve the index of the new share completed ")
        val emptyShare = result.data.shares.first()

        return if (retrieveShareDetails) {

            GetRemoteShareOperation(emptyShare.id).execute(client)
        } else {
            result
        }
    }

    private fun createFormBodyBuilder(): FormBody.Builder {
        val formBodyBuilder = FormBody.Builder()

        if (name != null) {
            formBodyBuilder.add(PARAM_NAME, name.orEmpty())
        }

        if (password != null) {
            formBodyBuilder.add(PARAM_PASSWORD, password.orEmpty())
        }

        if (expirationDateInMillis < INITIAL_EXPIRATION_DATE_IN_MILLIS) {

            formBodyBuilder.add(PARAM_EXPIRATION_DATE, "")

        } else if (expirationDateInMillis > INITIAL_EXPIRATION_DATE_IN_MILLIS) {

            val dateFormat = SimpleDateFormat(FORMAT_EXPIRATION_DATE, Locale.getDefault())
            val expirationDate = Calendar.getInstance()
            expirationDate.timeInMillis = expirationDateInMillis
            val formattedExpirationDate = dateFormat.format(expirationDate.time)
            formBodyBuilder.add(PARAM_EXPIRATION_DATE, formattedExpirationDate)
        } // else, ignore - no update


        if (permissions > DEFAULT_PERMISSION) {

            formBodyBuilder.add(PARAM_PERMISSIONS, permissions.toString())
        }

        return formBodyBuilder
    }


    override fun run(client: OwnCloudClient): RemoteOperationResult<ShareResponse> {
        val requestUri = buildRequestUri(client.baseUri)

        val formBodyBuilder = createFormBodyBuilder()

        val putMethod = PutMethod(URL(requestUri.toString()), formBodyBuilder.build()).apply {
            setRequestHeader(HttpConstants.CONTENT_TYPE_HEADER, HttpConstants.CONTENT_TYPE_URLENCODED_UTF8)
            addRequestHeader(OCS_API_HEADER, OCS_API_HEADER_VALUE)
        }

        return try {
            val status = client.executeHttpMethod(putMethod)
            val response = putMethod.getResponseBodyAsString()

            if (isSuccess(status)) {
                onRequestSuccessful(response)
            } else {
                onResultUnsuccessful(putMethod, response, status)
            }
        } catch (e: Exception) {
            Timber.e(e, "Exception while updating remote share")
            RemoteOperationResult(e)
        }
    }

    private fun isSuccess(status: Int): Boolean = status == HttpConstants.HTTP_OK

    companion object {

        private const val OCS_ROUTE = "ocs/v2.php/apps/files_sharing/api/v1/shares"

        private const val PARAM_NAME = "name"
        private const val PARAM_PASSWORD = "password"
        private const val PARAM_EXPIRATION_DATE = "expireDate"
        private const val PARAM_PERMISSIONS = "permissions"

        private const val FORMAT_EXPIRATION_DATE = "yyyy-MM-dd"
        private const val INITIAL_EXPIRATION_DATE_IN_MILLIS: Long = 0
    }
}
