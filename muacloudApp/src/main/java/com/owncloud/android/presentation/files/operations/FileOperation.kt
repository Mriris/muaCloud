

package com.owncloud.android.presentation.files.operations

import com.owncloud.android.domain.files.model.OCFile

sealed interface FileOperation {
    data class CopyOperation(
        val listOfFilesToCopy: List<OCFile>,
        val targetFolder: OCFile?,
        val replace: List<Boolean?> = emptyList(),
        val isUserLogged: Boolean,
    ) : FileOperation

    data class CreateFolder(val folderName: String, val parentFile: OCFile) : FileOperation
    data class MoveOperation(
        val listOfFilesToMove: List<OCFile>,
        val targetFolder: OCFile?,
        val replace: List<Boolean?> = emptyList(),
        val isUserLogged: Boolean,
    ) :
        FileOperation

    data class RemoveOperation(val listOfFilesToRemove: List<OCFile>, val removeOnlyLocalCopy: Boolean) : FileOperation
    data class RenameOperation(val ocFileToRename: OCFile, val newName: String) : FileOperation
    data class SynchronizeFileOperation(val fileToSync: OCFile, val accountName: String) : FileOperation
    data class SynchronizeFolderOperation(
        val folderToSync: OCFile,
        val accountName: String,
        val isActionSetFolderAvailableOfflineOrSynchronize: Boolean = false,
    ) : FileOperation
    data class RefreshFolderOperation(val folderToRefresh: OCFile, val shouldSyncContents: Boolean) : FileOperation
    data class CreateFileWithAppProviderOperation(val accountName: String, val parentContainerId: String, val filename: String) : FileOperation
    data class SetFilesAsAvailableOffline(val filesToUpdate: List<OCFile>) : FileOperation
    data class UnsetFilesAsAvailableOffline(val filesToUpdate: List<OCFile>) : FileOperation
}
