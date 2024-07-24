

package com.owncloud.android.domain.files.model

import com.owncloud.android.domain.spaces.model.OCSpace
import java.util.UUID

data class OCFileWithSyncInfo(
    val file: OCFile,
    val uploadWorkerUuid: UUID? = null,
    val downloadWorkerUuid: UUID? = null,
    val isSynchronizing: Boolean = false,
    val space: OCSpace? = null,
)
