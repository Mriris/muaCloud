
package com.owncloud.android.data.spaces.datasources

import com.owncloud.android.domain.spaces.model.OCSpace

interface RemoteSpacesDataSource {
    fun refreshSpacesForAccount(accountName: String): List<OCSpace>
}
