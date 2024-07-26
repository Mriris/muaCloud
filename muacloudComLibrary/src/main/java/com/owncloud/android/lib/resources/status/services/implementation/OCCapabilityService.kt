
package com.owncloud.android.lib.resources.status.services.implementation

import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.resources.status.GetRemoteCapabilitiesOperation
import com.owncloud.android.lib.resources.status.RemoteCapability
import com.owncloud.android.lib.resources.status.services.CapabilityService

class OCCapabilityService(override val client: OwnCloudClient) : CapabilityService {
    override fun getCapabilities(): RemoteOperationResult<RemoteCapability> =
        GetRemoteCapabilitiesOperation().execute(client)
}
