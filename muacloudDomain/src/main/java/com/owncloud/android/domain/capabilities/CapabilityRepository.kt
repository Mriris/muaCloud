

package com.owncloud.android.domain.capabilities

import androidx.lifecycle.LiveData
import com.owncloud.android.domain.capabilities.model.OCCapability

interface CapabilityRepository {
    fun getCapabilitiesAsLiveData(accountName: String): LiveData<OCCapability?>
    fun getStoredCapabilities(accountName: String): OCCapability?
    fun refreshCapabilitiesForAccount(accountName: String)
}
