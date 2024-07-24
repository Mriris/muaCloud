

package com.owncloud.android.data.sharing.shares.datasources

import androidx.lifecycle.LiveData
import com.owncloud.android.domain.sharing.shares.model.OCShare
import com.owncloud.android.domain.sharing.shares.model.ShareType

interface LocalShareDataSource {
    fun getSharesAsLiveData(
        filePath: String,
        accountName: String,
        shareTypes: List<ShareType>
    ): LiveData<List<OCShare>>

    fun getShareAsLiveData(
        remoteId: String
    ): LiveData<OCShare>

    fun insert(ocShare: OCShare): Long

    fun insert(ocShares: List<OCShare>): List<Long>

    fun update(ocShare: OCShare): Long

    fun replaceShares(ocShares: List<OCShare>): List<Long>

    fun deleteShare(remoteId: String): Int

    fun deleteSharesForFile(filePath: String, accountName: String)

    fun deleteSharesForAccount(accountName: String)
}
