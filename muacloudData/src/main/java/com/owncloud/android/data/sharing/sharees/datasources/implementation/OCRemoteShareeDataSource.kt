

package com.owncloud.android.data.sharing.sharees.datasources.implementation

import com.owncloud.android.data.ClientManager
import com.owncloud.android.data.executeRemoteOperation
import com.owncloud.android.data.sharing.sharees.datasources.RemoteShareeDataSource
import com.owncloud.android.data.sharing.sharees.datasources.mapper.RemoteShareeMapper
import com.owncloud.android.domain.sharing.sharees.model.OCSharee

class OCRemoteShareeDataSource(
    private val clientManager: ClientManager,
    private val shareeMapper: RemoteShareeMapper
) : RemoteShareeDataSource {

    override fun getSharees(
        searchString: String,
        page: Int,
        perPage: Int,
        accountName: String,
    ): List<OCSharee> =
        executeRemoteOperation {
            clientManager.getShareeService(accountName)
                .getSharees(
                    searchString = searchString,
                    page = page,
                    perPage = perPage
                )
        }.let {
            shareeMapper.toModel(it)
        }
}
