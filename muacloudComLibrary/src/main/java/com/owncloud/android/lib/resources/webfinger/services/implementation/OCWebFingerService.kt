
package com.owncloud.android.lib.resources.webfinger.services.implementation

import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.resources.webfinger.GetInstancesViaWebFingerOperation
import com.owncloud.android.lib.resources.webfinger.services.WebFingerService

class OCWebFingerService : WebFingerService {

    override fun getInstancesFromWebFinger(
        lookupServer: String,
        resource: String,
        rel: String,
        client: OwnCloudClient,
    ): RemoteOperationResult<List<String>> =
        GetInstancesViaWebFingerOperation(lookupServer, rel, resource).execute(client)
}
