

package com.owncloud.android.data.sharing.sharees.datasources

import com.owncloud.android.domain.sharing.sharees.model.OCSharee

interface RemoteShareeDataSource {
    fun getSharees(
        searchString: String,
        page: Int,
        perPage: Int,
        accountName: String,
    ): List<OCSharee>
}
