

package com.owncloud.android.domain.transfers.model

import android.os.Parcelable
import com.owncloud.android.domain.camerauploads.model.UploadBehavior
import kotlinx.parcelize.Parcelize
import java.io.File

@Parcelize
data class OCTransfer(
    var id: Long? = null,
    val localPath: String,
    val remotePath: String,
    val accountName: String,
    val fileSize: Long,
    var status: TransferStatus,
    val localBehaviour: UploadBehavior,
    val forceOverwrite: Boolean,
    val transferEndTimestamp: Long? = null,
    val lastResult: TransferResult? = null,
    val createdBy: UploadEnqueuedBy,
    val transferId: String? = null,
    val spaceId: String? = null,
) : Parcelable {
    init {
        if (!remotePath.startsWith(File.separator)) throw IllegalArgumentException("Remote path must be an absolute path in the local file system")
        if (accountName.isEmpty()) throw IllegalArgumentException("Invalid account name")
    }
}
