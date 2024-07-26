package com.owncloud.android.lib.resources.users

import at.bitfire.dav4jvm.Property
import at.bitfire.dav4jvm.property.QuotaAvailableBytes
import at.bitfire.dav4jvm.property.QuotaUsedBytes
import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.http.HttpConstants
import com.owncloud.android.lib.common.http.methods.webdav.DavConstants
import com.owncloud.android.lib.common.http.methods.webdav.DavUtils
import com.owncloud.android.lib.common.http.methods.webdav.PropfindMethod
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.operations.RemoteOperationResult.ResultCode
import com.owncloud.android.lib.resources.users.GetRemoteUserQuotaOperation.RemoteQuota
import timber.log.Timber
import java.net.URL
import kotlin.math.roundToLong


class GetRemoteUserQuotaOperation : RemoteOperation<RemoteQuota>() {
    override fun run(client: OwnCloudClient): RemoteOperationResult<RemoteQuota> =
        try {
            val propfindMethod = PropfindMethod(
                URL(client.userFilesWebDavUri.toString()),
                DavConstants.DEPTH_0,
                DavUtils.quotaPropSet
            )
            with(client.executeHttpMethod(propfindMethod)) {
                if (isSuccess(this)) {
                    RemoteOperationResult<RemoteQuota>(ResultCode.OK).apply {
                        data = readData(propfindMethod.root?.properties)
                    }.also {
                        Timber.i("Get quota completed: ${it.data} and message - HTTP status code: ${propfindMethod.statusCode}")
                    }
                } else { // synchronization failed
                    RemoteOperationResult<RemoteQuota>(propfindMethod).also {
                        Timber.e("Get quota without success: ${it.logMessage}")
                    }
                }
            }
        } catch (e: Exception) {
            RemoteOperationResult<RemoteQuota>(e).also {
                Timber.e(it.exception, "Get quota: ${it.logMessage}")
            }
        }

    private fun isSuccess(status: Int) = status == HttpConstants.HTTP_MULTI_STATUS || status == HttpConstants.HTTP_OK


    private fun readData(properties: List<Property>?): RemoteQuota {
        var quotaAvailable: Long = 0
        var quotaUsed: Long = 0

        if (properties == null) {

            Timber.d("Unable to get quota")
            return RemoteQuota(0, 0, 0, 0.0)
        }

        for (property in properties) {
            if (property is QuotaAvailableBytes) {
                quotaAvailable = property.quotaAvailableBytes
            }
            if (property is QuotaUsedBytes) {
                quotaUsed = property.quotaUsedBytes
            }
        }
        Timber.d("Quota used: $quotaUsed, QuotaAvailable: $quotaAvailable")




        return if (quotaAvailable < 0) {
            RemoteQuota(
                free = quotaAvailable,
                used = quotaUsed,
                total = 0,
                relative = 0.0
            )
        } else {
            val totalQuota = quotaAvailable + quotaUsed
            val roundedRelativeQuota = if (totalQuota > 0) {
                val relativeQuota = (quotaUsed * 100).toDouble() / totalQuota
                (relativeQuota * 100).roundToLong() / 100.0
            } else 0.0

            RemoteQuota(quotaAvailable, quotaUsed, totalQuota, roundedRelativeQuota)
        }
    }

    data class RemoteQuota(var free: Long, var used: Long, var total: Long, var relative: Double)
}
