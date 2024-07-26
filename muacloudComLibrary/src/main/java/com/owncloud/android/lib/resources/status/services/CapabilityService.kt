
package com.owncloud.android.lib.resources.status.services

import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.resources.Service
import com.owncloud.android.lib.resources.status.RemoteCapability

interface CapabilityService : Service {
    fun getCapabilities(): RemoteOperationResult<RemoteCapability>
}
