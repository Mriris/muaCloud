

package com.owncloud.android.domain.sharing.shares

import androidx.lifecycle.LiveData
import com.owncloud.android.domain.sharing.shares.model.OCShare
import com.owncloud.android.domain.sharing.shares.model.ShareType

interface ShareRepository {

    /******************************************************************************************************
     ******************************************* PRIVATE SHARES *******************************************
     ******************************************************************************************************/

    fun insertPrivateShare(
        filePath: String,
        shareType: ShareType,
        shareeName: String,
        permissions: Int,
        accountName: String
    )

    fun updatePrivateShare(
        remoteId: String,
        permissions: Int,
        accountName: String
    )

    /******************************************************************************************************
     ******************************************* PUBLIC SHARES ********************************************
     ******************************************************************************************************/

    fun insertPublicShare(
        filePath: String,
        permissions: Int,
        name: String,
        password: String,
        expirationTimeInMillis: Long,
        accountName: String
    )

    fun updatePublicShare(
        remoteId: String,
        name: String,
        password: String?,
        expirationDateInMillis: Long,
        permissions: Int,
        accountName: String
    )

    /******************************************************************************************************
     *********************************************** COMMON ***********************************************
     ******************************************************************************************************/

    fun getSharesAsLiveData(filePath: String, accountName: String): LiveData<List<OCShare>>

    fun getShareAsLiveData(remoteId: String): LiveData<OCShare>

    fun refreshSharesFromNetwork(filePath: String, accountName: String)

    fun deleteShare(remoteId: String, accountName: String)
}
