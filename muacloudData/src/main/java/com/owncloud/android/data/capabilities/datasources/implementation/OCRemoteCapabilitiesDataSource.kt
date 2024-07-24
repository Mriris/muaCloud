

package com.owncloud.android.data.capabilities.datasources.implementation

import com.owncloud.android.data.ClientManager
import com.owncloud.android.data.capabilities.datasources.RemoteCapabilitiesDataSource
import com.owncloud.android.data.capabilities.datasources.mapper.RemoteCapabilityMapper
import com.owncloud.android.data.executeRemoteOperation
import com.owncloud.android.domain.capabilities.model.OCCapability

class OCRemoteCapabilitiesDataSource(
    private val clientManager: ClientManager,
    private val remoteCapabilityMapper: RemoteCapabilityMapper
) : RemoteCapabilitiesDataSource {

    override fun getCapabilities(
        accountName: String
    ): OCCapability {
        executeRemoteOperation {
            clientManager.getCapabilityService(accountName).getCapabilities()
        }.let { remoteCapability ->
            return remoteCapabilityMapper.toModel(remoteCapability)!!.apply {
                this.accountName = accountName
            }
        }
    }
}
