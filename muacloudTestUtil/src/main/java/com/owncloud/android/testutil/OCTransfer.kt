

package com.owncloud.android.testutil

import com.owncloud.android.domain.camerauploads.model.UploadBehavior
import com.owncloud.android.domain.transfers.model.OCTransfer
import com.owncloud.android.domain.transfers.model.TransferStatus
import com.owncloud.android.domain.transfers.model.UploadEnqueuedBy

val OC_TRANSFER = OCTransfer(
    id = 0L,
    localPath = "/local/path",
    remotePath = "/remote/path",
    accountName = OC_ACCOUNT_NAME,
    fileSize = 1024L,
    status = TransferStatus.TRANSFER_IN_PROGRESS,
    localBehaviour = UploadBehavior.MOVE,
    forceOverwrite = true,
    createdBy = UploadEnqueuedBy.ENQUEUED_BY_USER
)
