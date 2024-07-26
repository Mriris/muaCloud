package com.owncloud.android.lib.resources.users

import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.http.HttpConstants
import com.owncloud.android.lib.common.http.methods.nonwebdav.GetMethod
import com.owncloud.android.lib.common.network.WebdavUtils
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.URL


class GetRemoteUserAvatarOperation(private val avatarDimension: Int) : RemoteOperation<RemoteAvatarData>() {
    override fun run(client: OwnCloudClient): RemoteOperationResult<RemoteAvatarData> {
        var inputStream: InputStream? = null
        var result: RemoteOperationResult<RemoteAvatarData>

        try {
            val endPoint =
                client.baseUri.toString() + NON_OFFICIAL_AVATAR_PATH + client.credentials.username + File.separator + avatarDimension
            Timber.d("avatar URI: %s", endPoint)

            val getMethod = GetMethod(URL(endPoint))

            val status = client.executeHttpMethod(getMethod)

            if (isSuccess(status)) {

                val contentLength = getMethod.getResponseHeader(HttpConstants.CONTENT_LENGTH_HEADER)?.toInt()

                val mimeType = getMethod.getResponseHeader(HttpConstants.CONTENT_TYPE_HEADER)

                if (mimeType == null || !mimeType.startsWith("image")) {
                    Timber.w("Not an image, failing with no avatar")
                    return RemoteOperationResult(RemoteOperationResult.ResultCode.FILE_NOT_FOUND)
                }

                inputStream = getMethod.getResponseBodyAsStream()
                val bytesArray = inputStream?.readBytes() ?: byteArrayOf()

                Timber.d("Avatar size: Bytes received ${bytesArray.size} of $contentLength")

                val etag = WebdavUtils.getEtagFromResponse(getMethod)
                if (etag.isEmpty()) {
                    Timber.w("Could not read Etag from avatar")
                }

                result = RemoteOperationResult(RemoteOperationResult.ResultCode.OK)
                result.setData(RemoteAvatarData(bytesArray, mimeType, etag))

            } else {
                result = RemoteOperationResult(getMethod)
                client.exhaustResponse(getMethod.getResponseBodyAsStream())
            }

        } catch (e: Exception) {
            result = RemoteOperationResult(e)
            Timber.e(e, "Exception while getting OC user avatar")

        } finally {
            try {
                client.exhaustResponse(inputStream)
                inputStream?.close()
            } catch (i: IOException) {
                Timber.e(i, "Unexpected exception closing input stream")
            }
        }

        return result
    }

    private fun isSuccess(status: Int) = status == HttpConstants.HTTP_OK

    companion object {
        private const val NON_OFFICIAL_AVATAR_PATH = "/index.php/avatar/"
    }
}
