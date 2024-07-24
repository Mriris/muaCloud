

package com.owncloud.android.domain.files.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class OCFileSyncInfo(
    val fileId: Long,
    val uploadWorkerUuid: UUID? = null,
    val downloadWorkerUuid: UUID? = null,
    val isSynchronizing: Boolean = false
) : Parcelable
