 
package com.owncloud.android.lib.resources.shares.services.implementation

import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.resources.shares.GetRemoteShareesOperation
import com.owncloud.android.lib.resources.shares.responses.ShareeOcsResponse
import com.owncloud.android.lib.resources.shares.services.ShareeService

class OCShareeService(override val client: OwnCloudClient) :
    ShareeService {
    override fun getSharees(
        searchString: String,
        page: Int,
        perPage: Int
    ): RemoteOperationResult<ShareeOcsResponse> =
        GetRemoteShareesOperation(
            searchString,
            page,
            perPage
        ).execute(client)
}
