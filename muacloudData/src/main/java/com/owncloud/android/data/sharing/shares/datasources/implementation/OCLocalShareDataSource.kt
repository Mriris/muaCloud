

package com.owncloud.android.data.sharing.shares.datasources.implementation

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.owncloud.android.data.sharing.shares.datasources.LocalShareDataSource
import com.owncloud.android.data.sharing.shares.db.OCShareDao
import com.owncloud.android.data.sharing.shares.db.OCShareEntity
import com.owncloud.android.domain.sharing.shares.model.OCShare
import com.owncloud.android.domain.sharing.shares.model.ShareType

class OCLocalShareDataSource(
    private val ocShareDao: OCShareDao,
) : LocalShareDataSource {

    override fun getSharesAsLiveData(
        filePath: String,
        accountName: String,
        shareTypes: List<ShareType>
    ): LiveData<List<OCShare>> =
        ocShareDao.getSharesAsLiveData(
            filePath,
            accountName,
            shareTypes.map { it.value },
        ).map { ocShareEntities ->
            ocShareEntities.map { ocShareEntity -> ocShareEntity.toModel() }
        }

    override fun getShareAsLiveData(remoteId: String): LiveData<OCShare> =
        ocShareDao.getShareAsLiveData(remoteId).map { ocShareEntity ->
            ocShareEntity.toModel()
        }

    override fun insert(ocShare: OCShare): Long =
        ocShareDao.insertOrReplace(
            ocShare.toEntity()
        )

    override fun insert(ocShares: List<OCShare>): List<Long> =
        ocShareDao.insertOrReplace(
            ocShares.map { ocShare -> ocShare.toEntity() }
        )

    override fun update(ocShare: OCShare): Long = ocShareDao.update(ocShare.toEntity())

    override fun replaceShares(ocShares: List<OCShare>): List<Long> =
        ocShareDao.replaceShares(
            ocShares.map { ocShare -> ocShare.toEntity() }
        )

    override fun deleteShare(remoteId: String): Int = ocShareDao.deleteShare(remoteId)

    override fun deleteSharesForFile(filePath: String, accountName: String) =
        ocShareDao.deleteSharesForFile(filePath, accountName)

    override fun deleteSharesForAccount(accountName: String) =
        ocShareDao.deleteSharesForAccount(accountName)

    companion object {
        @VisibleForTesting
        fun OCShareEntity.toModel(): OCShare =
            OCShare(
                id = id,
                shareType = ShareType.fromValue(shareType)!!,
                shareWith = shareWith,
                path = path,
                permissions = permissions,
                sharedDate = sharedDate,
                expirationDate = expirationDate,
                token = token,
                sharedWithDisplayName = sharedWithDisplayName,
                sharedWithAdditionalInfo = sharedWithAdditionalInfo,
                isFolder = isFolder,
                remoteId = remoteId,
                accountOwner = accountOwner,
                name = name,
                shareLink = shareLink
            )

        @VisibleForTesting
        fun OCShare.toEntity(): OCShareEntity =
            OCShareEntity(
                shareType = shareType.value,
                shareWith = shareWith,
                path = path,
                permissions = permissions,
                sharedDate = sharedDate,
                expirationDate = expirationDate,
                token = token,
                sharedWithDisplayName = sharedWithDisplayName,
                sharedWithAdditionalInfo = sharedWithAdditionalInfo,
                isFolder = isFolder,
                remoteId = remoteId,
                accountOwner = accountOwner,
                name = name,
                shareLink = shareLink
            )
    }
}
