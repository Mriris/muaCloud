

package com.owncloud.android.data.capabilities.datasources

import androidx.lifecycle.LiveData
import com.owncloud.android.domain.capabilities.model.OCCapability

interface LocalCapabilitiesDataSource {
    fun getCapabilitiesForAccountAsLiveData(
        accountName: String
    ): LiveData<OCCapability?>

    fun getCapabilitiesForAccount(
        accountName: String
    ): OCCapability?

    fun insertCapabilities(ocCapabilities: List<OCCapability>)

    fun deleteCapabilitiesForAccount(accountName: String)
}
