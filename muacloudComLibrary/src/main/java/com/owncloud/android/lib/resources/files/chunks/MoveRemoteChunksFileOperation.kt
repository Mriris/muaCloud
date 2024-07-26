package com.owncloud.android.lib.resources.files.chunks

import android.net.Uri
import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.http.HttpConstants
import com.owncloud.android.lib.common.http.methods.webdav.MoveMethod
import com.owncloud.android.lib.resources.files.MoveRemoteFileOperation


class MoveRemoteChunksFileOperation(
    sourceRemotePath: String,
    targetRemotePath: String,
    private val fileLastModificationTimestamp: String,
    private val fileLength: Long
) : MoveRemoteFileOperation(
    sourceRemotePath = sourceRemotePath,
    targetRemotePath = targetRemotePath,
) {

    override fun getSrcWebDavUriForClient(client: OwnCloudClient): Uri = client.uploadsWebDavUri

    override fun addRequestHeaders(moveMethod: MoveMethod) {
        super.addRequestHeaders(moveMethod)

        moveMethod.apply {
            addRequestHeader(HttpConstants.OC_X_OC_MTIME_HEADER, fileLastModificationTimestamp)
            addRequestHeader(HttpConstants.OC_TOTAL_LENGTH_HEADER, fileLength.toString())
        }
    }
}
