
package com.owncloud.android.lib.resources.webfinger.services

import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.operations.RemoteOperationResult

interface WebFingerService {
    fun getInstancesFromWebFinger(
        lookupServer: String,
        resource: String,
        rel: String,
        client: OwnCloudClient,
    ): RemoteOperationResult<List<String>>
}
