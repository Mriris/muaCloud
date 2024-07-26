
package com.owncloud.android.lib.resources.appregistry

import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.http.HttpConstants
import com.owncloud.android.lib.common.http.methods.nonwebdav.PostMethod
import com.owncloud.android.lib.common.network.WebdavUtils
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.operations.RemoteOperationResult.ResultCode
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import okhttp3.FormBody
import okhttp3.RequestBody
import timber.log.Timber
import java.net.URL
import java.util.concurrent.TimeUnit

class GetUrlToOpenInWebRemoteOperation(
    private val openWithWebEndpoint: String,
    private val fileId: String,
    private val appName: String,
) : RemoteOperation<String>() {

    override fun run(client: OwnCloudClient): RemoteOperationResult<String> {
        return try {

            val openInWebRequestBody = OpenInWebParams(fileId, appName).toRequestBody()

            val stringUrl =
                client.baseUri.toString() + WebdavUtils.encodePath(openWithWebEndpoint)

            val postMethod = PostMethod(URL(stringUrl), openInWebRequestBody).apply {
                setReadTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
                setConnectionTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
            }

            val status = client.executeHttpMethod(postMethod)
            Timber.d("Open in web for file: $fileId - $status${if (!isSuccess(status)) "(FAIL)" else ""}")

            if (isSuccess(status)) RemoteOperationResult<String>(ResultCode.OK).apply {
                val moshi = Moshi.Builder().build()
                val adapter: JsonAdapter<OpenInWebResponse> = moshi.adapter(OpenInWebResponse::class.java)

                data = postMethod.getResponseBodyAsString()?.let { adapter.fromJson(it)!!.uri }
            }
            else RemoteOperationResult<String>(postMethod).apply { data = "" }

        } catch (e: Exception) {
            val result = RemoteOperationResult<String>(e)
            Timber.e(e, "Open in web for file: $fileId failed")
            result
        }
    }

    private fun isSuccess(status: Int) = status == HttpConstants.HTTP_OK

    data class OpenInWebParams(
        val fileId: String,
        val appName: String,
    ) {
        fun toRequestBody(): RequestBody =
            FormBody.Builder()
                .add(PARAM_FILE_ID, fileId)
                .add(PARAM_APP_NAME, appName)
                .build()

        companion object {
            const val PARAM_FILE_ID = "file_id"
            const val PARAM_APP_NAME = "app_name"
        }
    }

    @JsonClass(generateAdapter = true)
    data class OpenInWebResponse(val uri: String)

    companion object {

        private const val TIMEOUT = 5_000L
    }
}
