
package com.owncloud.android.lib.resources.files.services.implementation

import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.resources.files.chunks.MoveRemoteChunksFileOperation
import com.owncloud.android.lib.resources.files.chunks.RemoveRemoteChunksFolderOperation
import com.owncloud.android.lib.resources.files.services.ChunkService

class OCChunkService(override val client: OwnCloudClient) : ChunkService {

    override fun removeFile(remotePath: String): RemoteOperationResult<Unit> =
        RemoveRemoteChunksFolderOperation(remotePath = remotePath).execute(client)

    override fun moveFile(
        sourceRemotePath: String,
        targetRemotePath: String,
        fileLastModificationTimestamp: String,
        fileLength: Long
    ): RemoteOperationResult<Unit> =
        MoveRemoteChunksFileOperation(
            sourceRemotePath = sourceRemotePath,
            targetRemotePath = targetRemotePath,
            fileLastModificationTimestamp = fileLastModificationTimestamp,
            fileLength = fileLength,
        ).execute(client)
}
