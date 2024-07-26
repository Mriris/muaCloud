package com.owncloud.android.lib.resources.status

import android.net.Uri
import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.operations.RemoteOperationResult.ResultCode
import com.owncloud.android.lib.resources.status.HttpScheme.HTTPS_PREFIX
import com.owncloud.android.lib.resources.status.HttpScheme.HTTP_PREFIX
import org.json.JSONException


class GetRemoteStatusOperation : RemoteOperation<RemoteServerInfo>() {

    public override fun run(client: OwnCloudClient): RemoteOperationResult<RemoteServerInfo> {
        if (!usesHttpOrHttps(client.baseUri)) {
            client.baseUri = buildFullHttpsUrl(client.baseUri)
        }
        return tryToConnect(client)
    }

    private fun updateClientBaseUrl(client: OwnCloudClient, newBaseUrl: String) {
        client.baseUri = Uri.parse(newBaseUrl)
    }


    private fun tryToConnect(client: OwnCloudClient): RemoteOperationResult<RemoteServerInfo> {
        val baseUrl = client.baseUri.toString()
        return try {
            val requester = StatusRequester()
            val requestResult = requester.request(baseUrl, client)
            val result = requester.handleRequestResult(requestResult, baseUrl)
            updateClientBaseUrl(client, result.data.baseUrl)
            return result
        } catch (e: JSONException) {
            RemoteOperationResult(ResultCode.INSTANCE_NOT_CONFIGURED)
        } catch (e: Exception) {
            RemoteOperationResult(e)
        }
    }

    companion object {
        fun usesHttpOrHttps(uri: Uri) =
            uri.toString().startsWith(HTTPS_PREFIX) || uri.toString().startsWith(HTTP_PREFIX)

        fun buildFullHttpsUrl(baseUri: Uri): Uri {
            if (usesHttpOrHttps(baseUri)) {
                return baseUri
            }
            return Uri.parse("$HTTPS_PREFIX$baseUri")
        }
    }
}
