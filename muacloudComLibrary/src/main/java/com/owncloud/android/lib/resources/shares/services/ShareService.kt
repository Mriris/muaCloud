
package com.owncloud.android.lib.resources.shares.services

import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.resources.Service
import com.owncloud.android.lib.resources.shares.ShareResponse
import com.owncloud.android.lib.resources.shares.ShareType

interface ShareService : Service {
    fun getShares(
        remoteFilePath: String,
        reshares: Boolean,
        subfiles: Boolean
    ): RemoteOperationResult<ShareResponse>

    fun insertShare(
        remoteFilePath: String,
        shareType: ShareType,
        shareWith: String,
        permissions: Int,
        name: String,
        password: String,
        expirationDate: Long,
    ): RemoteOperationResult<ShareResponse>

    fun updateShare(
        remoteId: String,
        name: String,
        password: String?,
        expirationDate: Long,
        permissions: Int,
    ): RemoteOperationResult<ShareResponse>

    fun deleteShare(remoteId: String): RemoteOperationResult<Unit>
}
