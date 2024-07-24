

package com.owncloud.android.data.files.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.owncloud.android.data.ProviderMeta.ProviderTableMeta.FILES_SYNC_TABLE_NAME
import java.util.UUID

@Entity(
    tableName = FILES_SYNC_TABLE_NAME,
    foreignKeys = [ForeignKey(
        entity = OCFileEntity::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("fileId"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class OCFileSyncEntity(
    @PrimaryKey val fileId: Long,
    val uploadWorkerUuid: UUID?,
    val downloadWorkerUuid: UUID?,
    val isSynchronizing: Boolean
)
