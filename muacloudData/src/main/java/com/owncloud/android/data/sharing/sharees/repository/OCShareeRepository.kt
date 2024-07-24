

package com.owncloud.android.data.sharing.sharees.repository

import com.owncloud.android.data.sharing.sharees.datasources.RemoteShareeDataSource
import com.owncloud.android.domain.sharing.sharees.ShareeRepository
import com.owncloud.android.domain.sharing.sharees.model.OCSharee

class OCShareeRepository(
    private val remoteShareeDataSource: RemoteShareeDataSource
) : ShareeRepository {

    override fun getSharees(
        searchString: String,
        page: Int,
        perPage: Int,
        accountName: String,
    ): List<OCSharee> = remoteShareeDataSource.getSharees(
        searchString = searchString,
        page = page,
        perPage = perPage,
        accountName = accountName,
    )
}
