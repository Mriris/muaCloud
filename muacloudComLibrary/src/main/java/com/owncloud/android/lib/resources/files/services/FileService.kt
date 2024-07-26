
package com.owncloud.android.lib.resources.files.services

import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.resources.Service
import com.owncloud.android.lib.resources.files.RemoteFile
import com.owncloud.android.lib.resources.files.RemoteMetaFile

interface FileService : Service {
    fun checkPathExistence(
        path: String,
        isUserLogged: Boolean,
        spaceWebDavUrl: String? = null,
    ): RemoteOperationResult<Boolean>

    fun copyFile(
        sourceRemotePath: String,
        targetRemotePath: String,
        sourceSpaceWebDavUrl: String?,
        targetSpaceWebDavUrl: String?,
        replace: Boolean,
    ): RemoteOperationResult<String?>

    fun createFolder(
        remotePath: String,
        createFullPath: Boolean,
        isChunkFolder: Boolean = false,
        spaceWebDavUrl: String? = null,
    ): RemoteOperationResult<Unit>

    fun downloadFile(
        remotePath: String,
        localTempPath: String
    ): RemoteOperationResult<Unit>

    fun moveFile(
        sourceRemotePath: String,
        targetRemotePath: String,
        spaceWebDavUrl: String?,
        replace: Boolean,
    ): RemoteOperationResult<Unit>

    fun readFile(
        remotePath: String,
        spaceWebDavUrl: String? = null,
    ): RemoteOperationResult<RemoteFile>

    fun refreshFolder(
        remotePath: String,
        spaceWebDavUrl: String? = null,
    ): RemoteOperationResult<ArrayList<RemoteFile>>

    fun removeFile(
        remotePath: String,
        spaceWebDavUrl: String? = null,
    ): RemoteOperationResult<Unit>

    fun renameFile(
        oldName: String,
        oldRemotePath: String,
        newName: String,
        isFolder: Boolean,
        spaceWebDavUrl: String? = null,
    ): RemoteOperationResult<Unit>

    fun getMetaFileInfo(
        fileId: String,
    ): RemoteOperationResult<RemoteMetaFile>

}
