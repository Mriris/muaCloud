package com.owncloud.android.lib.resources.files

import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.http.HttpConstants
import com.owncloud.android.lib.common.http.methods.webdav.DavUtils
import com.owncloud.android.lib.common.http.methods.webdav.PropfindMethod
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import timber.log.Timber
import java.net.URL
import java.util.concurrent.TimeUnit



class GetBaseUrlRemoteOperation : RemoteOperation<String?>() {
    override fun run(client: OwnCloudClient): RemoteOperationResult<String?> {
        return try {
            val stringUrl = client.baseFilesWebDavUri.toString()

            val propFindMethod = PropfindMethod(URL(stringUrl), 0, DavUtils.allPropSet).apply {
                setReadTimeout(TIMEOUT, TimeUnit.SECONDS)
                setConnectionTimeout(TIMEOUT, TimeUnit.SECONDS)
            }

            val status = client.executeHttpMethod(propFindMethod)

            if (isSuccess(status)) {
                RemoteOperationResult<String?>(RemoteOperationResult.ResultCode.OK).apply {
                    data = propFindMethod.getFinalUrl().toString()
                }
            } else {
                RemoteOperationResult<String?>(propFindMethod).apply {
                    data = null
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Could not get actuall (or redirected) base URL from base url (/).")
            RemoteOperationResult<String?>(e)
        }
    }

    private fun isSuccess(status: Int) = status == HttpConstants.HTTP_OK || status == HttpConstants.HTTP_MULTI_STATUS

    companion object {

        private const val TIMEOUT = 10_000L
    }
}
