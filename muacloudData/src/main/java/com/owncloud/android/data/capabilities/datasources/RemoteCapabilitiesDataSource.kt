

package com.owncloud.android.data.capabilities.datasources

import com.owncloud.android.domain.capabilities.model.OCCapability

interface RemoteCapabilitiesDataSource {
    fun getCapabilities(
        accountName: String
    ): OCCapability
}
