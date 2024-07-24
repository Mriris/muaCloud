

package com.owncloud.android.data.capabilities.repository

import androidx.lifecycle.LiveData
import com.owncloud.android.data.capabilities.datasources.LocalCapabilitiesDataSource
import com.owncloud.android.data.capabilities.datasources.RemoteCapabilitiesDataSource
import com.owncloud.android.domain.appregistry.AppRegistryRepository
import com.owncloud.android.domain.capabilities.CapabilityRepository
import com.owncloud.android.domain.capabilities.model.OCCapability

class OCCapabilityRepository(
    private val localCapabilitiesDataSource: LocalCapabilitiesDataSource,
    private val remoteCapabilitiesDataSource: RemoteCapabilitiesDataSource,
    private val appRegistryRepository: AppRegistryRepository,
) : CapabilityRepository {

    override fun getCapabilitiesAsLiveData(accountName: String): LiveData<OCCapability?> {
        return localCapabilitiesDataSource.getCapabilitiesForAccountAsLiveData(accountName)
    }

    override fun getStoredCapabilities(
        accountName: String
    ): OCCapability? = localCapabilitiesDataSource.getCapabilitiesForAccount(accountName)

    override fun refreshCapabilitiesForAccount(
        accountName: String
    ) {
        val capabilitiesFromNetwork = remoteCapabilitiesDataSource.getCapabilities(accountName)
        localCapabilitiesDataSource.insertCapabilities(listOf(capabilitiesFromNetwork))

        if (capabilitiesFromNetwork.filesAppProviders != null) {
            appRegistryRepository.refreshAppRegistryForAccount(accountName)
        }
    }
}
