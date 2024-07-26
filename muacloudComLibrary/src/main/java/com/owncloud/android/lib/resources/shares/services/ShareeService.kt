
package com.owncloud.android.lib.resources.shares.services

import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.resources.Service
import com.owncloud.android.lib.resources.shares.responses.ShareeOcsResponse

interface ShareeService : Service {
    fun getSharees(
        searchString: String,
        page: Int,
        perPage: Int
    ): RemoteOperationResult<ShareeOcsResponse>
}
