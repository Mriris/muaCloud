

package com.owncloud.android.data.sharing.shares.datasources

import com.owncloud.android.domain.sharing.shares.model.OCShare
import com.owncloud.android.domain.sharing.shares.model.ShareType
import com.owncloud.android.lib.resources.shares.RemoteShare.Companion.INIT_EXPIRATION_DATE_IN_MILLIS

interface RemoteShareDataSource {
    fun getShares(
        remoteFilePath: String,
        reshares: Boolean,
        subfiles: Boolean,
        accountName: String
    ): List<OCShare>

    fun insert(
        remoteFilePath: String,
        shareType: ShareType,
        shareWith: String,
        permissions: Int,
        name: String = "",
        password: String = "",
        expirationDate: Long = INIT_EXPIRATION_DATE_IN_MILLIS,
        accountName: String
    ): OCShare

    fun updateShare(
        remoteId: String,
        name: String = "",
        password: String? = "",
        expirationDateInMillis: Long = INIT_EXPIRATION_DATE_IN_MILLIS,
        permissions: Int,
        accountName: String
    ): OCShare

    fun deleteShare(
        remoteId: String,
        accountName: String,
    )
}
