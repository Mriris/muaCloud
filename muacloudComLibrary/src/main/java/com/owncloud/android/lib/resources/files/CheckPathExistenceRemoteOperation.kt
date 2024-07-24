/* ownCloud Android Library is available under MIT license
*   Copyright (C) 2023 ownCloud GmbH.
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
            /* PROPFIND method
             * 404 NOT FOUND: path doesn't exist,
             * 207 MULTI_STATUS: path exists.
             */
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
