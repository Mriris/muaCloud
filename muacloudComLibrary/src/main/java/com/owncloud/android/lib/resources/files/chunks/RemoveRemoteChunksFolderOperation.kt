package com.owncloud.android.lib.resources.files.chunks

import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.resources.files.RemoveRemoteFileOperation

class RemoveRemoteChunksFolderOperation(remotePath: String) : RemoveRemoteFileOperation(remotePath) {
    override fun getSrcWebDavUriForClient(client: OwnCloudClient): String = client.uploadsWebDavUri.toString()
}
