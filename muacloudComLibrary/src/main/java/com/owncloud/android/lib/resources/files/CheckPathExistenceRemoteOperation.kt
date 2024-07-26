package com.owncloud.android.lib.resources.files

import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.http.HttpConstants
import com.owncloud.android.lib.common.http.methods.webdav.DavUtils.allPropSet
import com.owncloud.android.lib.common.http.methods.webdav.PropfindMethod
import com.owncloud.android.lib.common.network.WebdavUtils
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.operations.RemoteOperationResult.ResultCode
import timber.log.Timber
import java.net.URL
import java.util.concurrent.TimeUnit


class CheckPathExistenceRemoteOperation(
    val remotePath: String? = "",
    val isUserLoggedIn: Boolean,
    val spaceWebDavUrl: String? = null,
) : RemoteOperation<Boolean>() {

    override fun run(client: OwnCloudClient): RemoteOperationResult<Boolean> {
        val baseStringUrl = spaceWebDavUrl ?: if (isUserLoggedIn) client.userFilesWebDavUri.toString() else client.baseFilesWebDavUri.toString()
        val stringUrl = if (isUserLoggedIn) baseStringUrl + WebdavUtils.encodePath(remotePath) else baseStringUrl

        return try {
            val propFindMethod = PropfindMethod(URL(stringUrl), 0, allPropSet).apply {
                setReadTimeout(TIMEOUT.toLong(), TimeUnit.SECONDS)
                setConnectionTimeout(TIMEOUT.toLong(), TimeUnit.SECONDS)
            }

            val status = client.executeHttpMethod(propFindMethod)
                        Timber.d(
                "Existence check for $stringUrl finished with HTTP status $status${if (!isSuccess(status)) "(FAIL)" else ""}"
            )
            if (isSuccess(status)) RemoteOperationResult<Boolean>(ResultCode.OK).apply { data = true }
            else RemoteOperationResult<Boolean>(propFindMethod).apply { data = false }

        } catch (e: Exception) {
            val result = RemoteOperationResult<Boolean>(e)
            Timber.e(
                e,
                "Existence check for $stringUrl : ${result.logMessage}"
            )
            result.data = false
            result
        }
    }

    private fun isSuccess(status: Int) = status == HttpConstants.HTTP_OK || status == HttpConstants.HTTP_MULTI_STATUS

    companion object {

        private const val TIMEOUT = 10000
    }
}
