
package com.owncloud.android.lib.resources.files.services

import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.resources.Service

interface ChunkService : Service {
    fun removeFile(
        remotePath: String
    ): RemoteOperationResult<Unit>

    fun moveFile(
        sourceRemotePath: String,
        targetRemotePath: String,
        fileLastModificationTimestamp: String,
        fileLength: Long
    ): RemoteOperationResult<Unit>
}
