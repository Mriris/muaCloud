
package com.owncloud.android.lib.resources.shares.services.implementation

import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.resources.shares.CreateRemoteShareOperation
import com.owncloud.android.lib.resources.shares.GetRemoteSharesForFileOperation
import com.owncloud.android.lib.resources.shares.RemoveRemoteShareOperation
import com.owncloud.android.lib.resources.shares.ShareResponse
import com.owncloud.android.lib.resources.shares.ShareType
import com.owncloud.android.lib.resources.shares.UpdateRemoteShareOperation
import com.owncloud.android.lib.resources.shares.services.ShareService

class OCShareService(override val client: OwnCloudClient) : ShareService {
    override fun getShares(
        remoteFilePath: String,
        reshares: Boolean,
        subfiles: Boolean
    ): RemoteOperationResult<ShareResponse> = GetRemoteSharesForFileOperation(
        remoteFilePath,
        reshares,
        subfiles
    ).execute(client)

    override fun insertShare(
        remoteFilePath: String,
        shareType: ShareType,
        shareWith: String,
        permissions: Int,
        name: String,
        password: String,
        expirationDate: Long,
    ): RemoteOperationResult<ShareResponse> =
        CreateRemoteShareOperation(
            remoteFilePath,
            shareType,
            shareWith,
            permissions
        ).apply {
            this.name = name
            this.password = password
            this.expirationDateInMillis = expirationDate
            this.retrieveShareDetails = true
        }.execute(client)

    override fun updateShare(
        remoteId: String,
        name: String,
        password: String?,
        expirationDate: Long,
        permissions: Int,
    ): RemoteOperationResult<ShareResponse> =
        UpdateRemoteShareOperation(
            remoteId
        ).apply {
            this.name = name
            this.password = password
            this.expirationDateInMillis = expirationDate
            this.permissions = permissions
            this.retrieveShareDetails = true
        }.execute(client)

    override fun deleteShare(remoteId: String): RemoteOperationResult<Unit> =
        RemoveRemoteShareOperation(
            remoteId
        ).execute(client)
}
