/* ownCloud Android Library is available under MIT license
 *   @author masensio
 *   @author David A. Velasco
 *   @author David González Verdugo
 *   @author Fernando Sanz Velasco
 *   Copyright (C) 2021 ownCloud GmbH
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *   EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 *   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 *   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *   THE SOFTWARE.
 *
 */

package com.owncloud.android.lib.resources.shares

import android.net.Uri
import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.http.HttpConstants
import com.owncloud.android.lib.common.http.HttpConstants.PARAM_FORMAT
import com.owncloud.android.lib.common.http.HttpConstants.VALUE_FORMAT
import com.owncloud.android.lib.common.http.methods.nonwebdav.PostMethod
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.resources.CommonOcsResponse
import com.owncloud.android.lib.resources.shares.RemoteShare.Companion.INIT_EXPIRATION_DATE_IN_MILLIS
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




class CreateRemoteShareOperation(
    private val remoteFilePath: String,
    private val shareType: ShareType,
    private val shareWith: String,
    private val permissions: Int
) : RemoteOperation<ShareResponse>() {

    var name = "" // Name to set for the public link

    var password: String = "" // Password to set for the public link

    var expirationDateInMillis: Long = INIT_EXPIRATION_DATE_IN_MILLIS // Expiration date to set for the public link

    var retrieveShareDetails = false // To retrieve more info about the just created share

    private fun buildRequestUri(baseUri: Uri) =
        baseUri.buildUpon()
            .appendEncodedPath(OCS_ROUTE)
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
        method: PostMethod,
        response: String?,
        status: Int
    ): RemoteOperationResult<ShareResponse> {
        Timber.e("Failed response while while creating new remote share operation ")
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
        Timber.d("*** Creating new remote share operation completed ")

        val emptyShare = result.data.shares.first()

        return if (retrieveShareDetails) {
            // retrieve more info - PUT only returns the index of the new share
            GetRemoteShareOperation(emptyShare.id).execute(client)
        } else {
            result
        }
    }

    private fun createFormBody(): FormBody {
        val formBodyBuilder = FormBody.Builder()
            .add(PARAM_PATH, remoteFilePath)
            .add(PARAM_SHARE_TYPE, shareType.value.toString())
            .add(PARAM_SHARE_WITH, shareWith)

        if (name.isNotEmpty()) {
            formBodyBuilder.add(PARAM_NAME, name)
        }

        if (expirationDateInMillis > INIT_EXPIRATION_DATE_IN_MILLIS) {
            val dateFormat = SimpleDateFormat(FORMAT_EXPIRATION_DATE, Locale.getDefault())
            val expirationDate = Calendar.getInstance()
            expirationDate.timeInMillis = expirationDateInMillis
            val formattedExpirationDate = dateFormat.format(expirationDate.time)
            formBodyBuilder.add(PARAM_EXPIRATION_DATE, formattedExpirationDate)
        }

        if (password.isNotEmpty()) {
            formBodyBuilder.add(PARAM_PASSWORD, password)
        }

        if (RemoteShare.DEFAULT_PERMISSION != permissions) {
            formBodyBuilder.add(PARAM_PERMISSIONS, permissions.toString())
        }

        return formBodyBuilder.build()
    }

    override fun run(client: OwnCloudClient): RemoteOperationResult<ShareResponse> {
        val requestUri = buildRequestUri(client.baseUri)

        val postMethod = PostMethod(URL(requestUri.toString()), createFormBody()).apply {
            setRequestHeader(HttpConstants.CONTENT_TYPE_HEADER, HttpConstants.CONTENT_TYPE_URLENCODED_UTF8)
            addRequestHeader(OCS_API_HEADER, OCS_API_HEADER_VALUE)
        }

        return try {
            val status = client.executeHttpMethod(postMethod)
            val response = postMethod.getResponseBodyAsString()

            if (isSuccess(status)) {
                onRequestSuccessful(response)
            } else {
                onResultUnsuccessful(postMethod, response, status)
            }

        } catch (e: Exception) {
            Timber.e(e, "Exception while creating new remote share operation ")
            RemoteOperationResult(e)
        }
    }

    private fun isSuccess(status: Int): Boolean = status == HttpConstants.HTTP_OK

    companion object {

        //OCS Route
        private const val OCS_ROUTE = "ocs/v2.php/apps/files_sharing/api/v1/shares"

        //Arguments - names

        private const val PARAM_NAME = "name"
        private const val PARAM_EXPIRATION_DATE = "expireDate"
        private const val PARAM_PATH = "path"
        private const val PARAM_SHARE_TYPE = "shareType"
        private const val PARAM_SHARE_WITH = "shareWith"
        private const val PARAM_PASSWORD = "password"
        private const val PARAM_PERMISSIONS = "permissions"

        //Arguments - constant values
        private const val FORMAT_EXPIRATION_DATE = "yyyy-MM-dd"
    }
}
