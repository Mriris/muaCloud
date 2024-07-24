

package com.owncloud.android.data.files.db

import androidx.room.Embedded
import androidx.room.Relation
import com.owncloud.android.data.spaces.db.SpacesEntity

data class OCFileAndFileSync(
    @Embedded val file: OCFileEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "fileId"
    )
    val fileSync: OCFileSyncEntity?,
    @Relation(
        parentColumn = "spaceId",
        entityColumn = "space_id"
    )
    val space: SpacesEntity? = null,
)
