

package com.owncloud.android.data.sharing.shares.db

import android.content.ContentValues
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.owncloud.android.data.ProviderMeta.ProviderTableMeta


@Entity(tableName = ProviderTableMeta.OCSHARES_TABLE_NAME)
data class OCShareEntity(
    @ColumnInfo(name = ProviderTableMeta.OCSHARES_SHARE_TYPE)
    val shareType: Int,
    @ColumnInfo(name = ProviderTableMeta.OCSHARES_SHARE_WITH)
    val shareWith: String?,
    @ColumnInfo(name = ProviderTableMeta.OCSHARES_PATH)
    val path: String,
    @ColumnInfo(name = ProviderTableMeta.OCSHARES_PERMISSIONS)
    val permissions: Int,
    @ColumnInfo(name = ProviderTableMeta.OCSHARES_SHARED_DATE)
    val sharedDate: Long,
    @ColumnInfo(name = ProviderTableMeta.OCSHARES_EXPIRATION_DATE)
    val expirationDate: Long,
    @ColumnInfo(name = ProviderTableMeta.OCSHARES_TOKEN)
    val token: String?,
    @ColumnInfo(name = ProviderTableMeta.OCSHARES_SHARE_WITH_DISPLAY_NAME)
    val sharedWithDisplayName: String?,
    @ColumnInfo(name = ProviderTableMeta.OCSHARES_SHARE_WITH_ADDITIONAL_INFO)
    val sharedWithAdditionalInfo: String?,
    @ColumnInfo(name = ProviderTableMeta.OCSHARES_IS_DIRECTORY)
    val isFolder: Boolean,
    @ColumnInfo(name = ProviderTableMeta.OCSHARES_ID_REMOTE_SHARED)
    val remoteId: String,
    @ColumnInfo(name = ProviderTableMeta.OCSHARES_ACCOUNT_OWNER)
    var accountOwner: String = "",
    @ColumnInfo(name = ProviderTableMeta.OCSHARES_NAME)
    val name: String?,
    @ColumnInfo(name = ProviderTableMeta.OCSHARES_URL)
    val shareLink: String?
) {
    @PrimaryKey(autoGenerate = true) var id: Int = 0

    companion object {
        fun fromContentValues(values: ContentValues): OCShareEntity {
            return OCShareEntity(
                values.getAsInteger(ProviderTableMeta.OCSHARES_SHARE_TYPE),
                values.getAsString(ProviderTableMeta.OCSHARES_SHARE_WITH),
                values.getAsString(ProviderTableMeta.OCSHARES_PATH),
                values.getAsInteger(ProviderTableMeta.OCSHARES_PERMISSIONS),
                values.getAsLong(ProviderTableMeta.OCSHARES_SHARED_DATE),
                values.getAsLong(ProviderTableMeta.OCSHARES_EXPIRATION_DATE),
                values.getAsString(ProviderTableMeta.OCSHARES_TOKEN),
                values.getAsString(ProviderTableMeta.OCSHARES_SHARE_WITH_DISPLAY_NAME),
                values.getAsString(ProviderTableMeta.OCSHARES_SHARE_WITH_ADDITIONAL_INFO),
                values.getAsBoolean(ProviderTableMeta.OCSHARES_IS_DIRECTORY),
                values.getAsLong(ProviderTableMeta.OCSHARES_ID_REMOTE_SHARED).toString(),
                values.getAsString(ProviderTableMeta.OCSHARES_ACCOUNT_OWNER),
                values.getAsString(ProviderTableMeta.OCSHARES_NAME),
                values.getAsString(ProviderTableMeta.OCSHARES_URL)
            )
        }
    }
}
