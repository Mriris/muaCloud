

package com.owncloud.android.data.files.repository

import com.owncloud.android.data.files.datasources.LocalFileDataSource
import com.owncloud.android.data.files.datasources.RemoteFileDataSource
import com.owncloud.android.data.providers.LocalStorageProvider
import com.owncloud.android.data.spaces.datasources.LocalSpacesDataSource
import com.owncloud.android.domain.availableoffline.model.AvailableOfflineStatus
import com.owncloud.android.domain.availableoffline.model.AvailableOfflineStatus.AVAILABLE_OFFLINE_PARENT
import com.owncloud.android.domain.availableoffline.model.AvailableOfflineStatus.NOT_AVAILABLE_OFFLINE
import com.owncloud.android.domain.exceptions.ConflictException
import com.owncloud.android.domain.exceptions.FileAlreadyExistsException
import com.owncloud.android.domain.exceptions.FileNotFoundException
import com.owncloud.android.domain.files.FileRepository
import com.owncloud.android.domain.files.model.FileListOption
import com.owncloud.android.domain.files.model.MIME_DIR
import com.owncloud.android.domain.files.model.OCFile
import com.owncloud.android.domain.files.model.OCFile.Companion.PATH_SEPARATOR
import com.owncloud.android.domain.files.model.OCFile.Companion.ROOT_PATH
import com.owncloud.android.domain.files.model.OCFileWithSyncInfo
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import java.io.File
import java.util.UUID

class OCFileRepository(
    private val localFileDataSource: LocalFileDataSource,
    private val remoteFileDataSource: RemoteFileDataSource,
    private val localSpacesDataSource: LocalSpacesDataSource,
    private val localStorageProvider: LocalStorageProvider,
) : FileRepository {
    override fun createFolder(
        remotePath: String,
        parentFolder: OCFile,
    ) {
        val spaceWebDavUrl = localSpacesDataSource.getWebDavUrlForSpace(parentFolder.spaceId, parentFolder.owner)

        remoteFileDataSource.createFolder(
            remotePath = remotePath,
            createFullPath = false,
            isChunksFolder = false,
            accountName = parentFolder.owner,
            spaceWebDavUrl = spaceWebDavUrl,
        ).also {
            localFileDataSource.saveFilesInFolderAndReturnTheFilesThatChanged(
                folder = parentFolder,
                listOfFiles = listOf(
                    OCFile(
                        remotePath = remotePath,
                        owner = parentFolder.owner,
                        modificationTimestamp = System.currentTimeMillis(),
                        length = 0,
                        mimeType = MIME_DIR,
                        spaceId = parentFolder.spaceId,
                        permissions = "CK" // To be able to write inside a folder before the fetch is done
                    )
                )
            )
        }
    }

    override fun copyFile(listOfFilesToCopy: List<OCFile>, targetFolder: OCFile, replace: List<Boolean?>, isUserLogged: Boolean): List<OCFile> {
        val sourceSpaceWebDavUrl = localSpacesDataSource.getWebDavUrlForSpace(listOfFilesToCopy[0].spaceId, listOfFilesToCopy[0].owner)
        val targetSpaceWebDavUrl = localSpacesDataSource.getWebDavUrlForSpace(targetFolder.spaceId, targetFolder.owner)
        val filesNeedAction = mutableListOf<OCFile>()

        listOfFilesToCopy.forEachIndexed forEach@{ position, ocFile ->

            val expectedRemotePath: String = targetFolder.remotePath + ocFile.fileName

            val finalRemotePath: String? =
                getFinalRemotePath(
                    replace = replace,
                    expectedRemotePath = expectedRemotePath,
                    targetFolder = targetFolder,
                    targetSpaceWebDavUrl = targetSpaceWebDavUrl,
                    filesNeedsAction = filesNeedAction,
                    ocFile = ocFile,
                    position = position,
                    isUserLogged = isUserLogged,
                )
            if (finalRemotePath != null && (replace.isEmpty() || replace[position] != null)) {

                val remoteId = try {
                    remoteFileDataSource.copyFile(
                        sourceRemotePath = ocFile.remotePath,
                        targetRemotePath = finalRemotePath,
                        accountName = ocFile.owner,
                        sourceSpaceWebDavUrl = sourceSpaceWebDavUrl,
                        targetSpaceWebDavUrl = targetSpaceWebDavUrl,
                        replace = if (replace.isEmpty()) false else replace[position]!!,
                    )
                } catch (targetNodeDoesNotExist: ConflictException) {

                    deleteLocalFolderRecursively(ocFile = targetFolder, onlyFromLocalStorage = false)
                    throw targetNodeDoesNotExist
                } catch (sourceFileDoesNotExist: FileNotFoundException) {

                    if (ocFile.isFolder) {
                        deleteLocalFolderRecursively(ocFile = ocFile, onlyFromLocalStorage = false)
                    } else {
                        deleteLocalFile(
                            ocFile = ocFile,
                            onlyFromLocalStorage = false
                        )
                    }
                    if (listOfFilesToCopy.size == 1) {
                        throw sourceFileDoesNotExist
                    } else {
                        return@forEach
                    }
                }

                remoteId?.let {
                    localFileDataSource.copyFile(
                        sourceFile = ocFile,
                        targetFolder = targetFolder,
                        finalRemotePath = finalRemotePath,
                        remoteId = it,
                        replace = if (replace.isEmpty()) {
                            null
                        } else {
                            replace[position]
                        },
                    )
                }
            }
        }
        return filesNeedAction
    }

    override fun getFileById(fileId: Long): OCFile? =
        localFileDataSource.getFileById(fileId)

    override fun getFileWithSyncInfoByIdAsFlow(fileId: Long): Flow<OCFileWithSyncInfo?> =
        localFileDataSource.getFileWithSyncInfoByIdAsFlow(fileId)

    override fun getFileByIdAsFlow(fileId: Long): Flow<OCFile?> =
        localFileDataSource.getFileByIdAsFlow(fileId)

    override fun getFileByRemotePath(remotePath: String, owner: String, spaceId: String?): OCFile? =
        localFileDataSource.getFileByRemotePath(remotePath, owner, spaceId)

    override fun getFileFromRemoteId(fileId: String, accountName: String): OCFile? {
        val metaFile = remoteFileDataSource.getMetaFile(fileId, accountName)
        val remotePath = metaFile.path!!

        val splitPath = remotePath.split(PATH_SEPARATOR)
        var containerFolder = listOf<OCFile>()
        for (i in 0..splitPath.size - 2) {
            var path = splitPath[0]
            for (j in 1..i) {
                path += "$PATH_SEPARATOR${splitPath[j]}"
            }
            containerFolder = refreshFolder(path, accountName, metaFile.spaceId)
        }
        refreshFolder(remotePath, accountName, metaFile.spaceId)
        return if (remotePath == ROOT_PATH) {
            getFileByRemotePath(remotePath, accountName, metaFile.spaceId)
        } else {
            containerFolder.find { file ->
                if (file.isFolder) {
                    file.remotePath.dropLast(1)
                } else {
                    file.remotePath
                } == remotePath
            }
        }
    }

    override fun getPersonalRootFolderForAccount(owner: String): OCFile {
        val personalSpace = localSpacesDataSource.getPersonalSpaceForAccount(owner)
        if (personalSpace == null) {
            val legacyRootFolder = localFileDataSource.getFileByRemotePath(remotePath = ROOT_PATH, owner = owner, spaceId = null)
            try {
                return legacyRootFolder ?: throw IllegalStateException("LegacyRootFolder not found")
            } catch (e: IllegalStateException) {
                Timber.i("There was an error: $e")
            }
        }

        val personalRootFolder = localFileDataSource.getFileByRemotePath(remotePath = ROOT_PATH, owner = owner, spaceId = personalSpace?.root?.id)
        return personalRootFolder!!
    }

    override fun getSharesRootFolderForAccount(owner: String): OCFile? {
        val sharesSpaces = localSpacesDataSource.getSharesSpaceForAccount(owner) ?: return null

        val personalRootFolder = localFileDataSource.getFileByRemotePath(remotePath = ROOT_PATH, owner = owner, spaceId = sharesSpaces.root.id)
        return personalRootFolder!!
    }

    override fun getSearchFolderContent(fileListOption: FileListOption, folderId: Long, search: String): List<OCFile> =
        when (fileListOption) {
            FileListOption.ALL_FILES -> localFileDataSource.getSearchFolderContent(folderId, search)
            FileListOption.SPACES_LIST -> emptyList()
            FileListOption.AV_OFFLINE -> localFileDataSource.getSearchAvailableOfflineFolderContent(folderId, search)
            FileListOption.SHARED_BY_LINK -> localFileDataSource.getSearchSharedByLinkFolderContent(folderId, search)
        }

    override fun getFolderContent(folderId: Long): List<OCFile> =
        localFileDataSource.getFolderContent(folderId)

    override fun getFolderContentWithSyncInfoAsFlow(folderId: Long): Flow<List<OCFileWithSyncInfo>> =
        localFileDataSource.getFolderContentWithSyncInfoAsFlow(folderId)

    override fun getFolderImages(folderId: Long): List<OCFile> =
        localFileDataSource.getFolderImages(folderId)

    override fun getSharedByLinkWithSyncInfoForAccountAsFlow(owner: String): Flow<List<OCFileWithSyncInfo>> =
        localFileDataSource.getSharedByLinkWithSyncInfoForAccountAsFlow(owner)

    override fun getFilesWithSyncInfoAvailableOfflineFromAccountAsFlow(owner: String): Flow<List<OCFileWithSyncInfo>> =
        localFileDataSource.getFilesWithSyncInfoAvailableOfflineFromAccountAsFlow(owner)

    override fun getFilesAvailableOfflineFromAccount(owner: String): List<OCFile> =
        localFileDataSource.getFilesAvailableOfflineFromAccount(owner)

    override fun getFilesAvailableOfflineFromEveryAccount(): List<OCFile> =
        localFileDataSource.getFilesAvailableOfflineFromEveryAccount()

    override fun getDownloadedFilesForAccount(owner: String): List<OCFile> = localFileDataSource.getDownloadedFilesForAccount(owner)

    override fun getFilesWithLastUsageOlderThanGivenTime(milliseconds: Long): List<OCFile> =
        localFileDataSource.getFilesWithLastUsageOlderThanGivenTime(milliseconds)

    override fun moveFile(listOfFilesToMove: List<OCFile>, targetFolder: OCFile, replace: List<Boolean?>, isUserLogged: Boolean): List<OCFile> {
        val targetSpaceWebDavUrl = localSpacesDataSource.getWebDavUrlForSpace(targetFolder.spaceId, targetFolder.owner)
        val filesNeedsAction = mutableListOf<OCFile>()


        listOfFilesToMove.forEachIndexed forEach@{ position, ocFile ->

            val expectedRemotePath: String = targetFolder.remotePath + ocFile.fileName
            val finalRemotePath: String? =
                getFinalRemotePath(
                    replace = replace,
                    expectedRemotePath = expectedRemotePath,
                    targetFolder = targetFolder,
                    targetSpaceWebDavUrl = targetSpaceWebDavUrl,
                    filesNeedsAction = filesNeedsAction,
                    ocFile = ocFile,
                    position = position,
                    isUserLogged = isUserLogged,
                )

            if (finalRemotePath != null && (replace.isEmpty() || replace[position] != null)) {
                val finalStoragePath: String = localStorageProvider.getDefaultSavePathFor(targetFolder.owner, finalRemotePath, targetFolder.spaceId)

                try {
                    remoteFileDataSource.moveFile(
                        sourceRemotePath = ocFile.remotePath,
                        targetRemotePath = finalRemotePath,
                        accountName = ocFile.owner,
                        spaceWebDavUrl = targetSpaceWebDavUrl,
                        replace = if (replace.isEmpty()) false else replace[position]!!,
                    )
                } catch (targetNodeDoesNotExist: ConflictException) {

                    deleteLocalFolderRecursively(ocFile = targetFolder, onlyFromLocalStorage = false)
                    throw targetNodeDoesNotExist
                } catch (sourceFileDoesNotExist: FileNotFoundException) {

                    if (ocFile.isFolder) {
                        deleteLocalFolderRecursively(ocFile = ocFile, onlyFromLocalStorage = false)
                    } else {
                        deleteLocalFile(
                            ocFile = ocFile,
                            onlyFromLocalStorage = false
                        )
                    }
                    if (listOfFilesToMove.size == 1) {
                        throw sourceFileDoesNotExist
                    } else {
                        return@forEach
                    }
                }

                ocFile.etagInConflict?.let {
                    localFileDataSource.cleanConflict(ocFile.id!!)
                }

                localFileDataSource.moveFile(
                    sourceFile = ocFile,
                    targetFolder = targetFolder,
                    finalRemotePath = finalRemotePath,
                    finalStoragePath = finalStoragePath
                )

                ocFile.etagInConflict?.let {
                    localFileDataSource.saveConflict(ocFile.id!!, it)
                }

                localStorageProvider.moveLocalFile(ocFile, finalStoragePath)
            }
        }
        return filesNeedsAction
    }

    private fun getFinalRemotePath(
        replace: List<Boolean?>,
        expectedRemotePath: String,
        targetFolder: OCFile,
        targetSpaceWebDavUrl: String?,
        filesNeedsAction: MutableList<OCFile>,
        ocFile: OCFile,
        position: Int,
        isUserLogged: Boolean,
    ) =
        if (replace.isEmpty()) {
            val pathExists = remoteFileDataSource.checkPathExistence(
                path = expectedRemotePath,
                isUserLogged = isUserLogged,
                accountName = targetFolder.owner,
                spaceWebDavUrl = targetSpaceWebDavUrl,
            )
            if (pathExists) {
                filesNeedsAction.add(ocFile)
                null
            } else {
                if (ocFile.isFolder) expectedRemotePath.plus(File.separator) else expectedRemotePath
            }
        } else {
            if (replace[position] == true) {
                if (ocFile.isFolder) expectedRemotePath.plus(File.separator) else expectedRemotePath
            } else if (replace[position] == false) {
                remoteFileDataSource.getAvailableRemotePath(
                    remotePath = expectedRemotePath,
                    accountName = targetFolder.owner,
                    spaceWebDavUrl = targetSpaceWebDavUrl,
                    isUserLogged = isUserLogged,
                ).let {
                    if (ocFile.isFolder) it.plus(File.separator) else it
                }
            } else {
                null
            }
        }

    override fun readFile(remotePath: String, accountName: String, spaceId: String?): OCFile {
        val spaceWebDavUrl = localSpacesDataSource.getWebDavUrlForSpace(spaceId, accountName)

        return remoteFileDataSource.readFile(remotePath, accountName, spaceWebDavUrl).copy(spaceId = spaceId)
    }

    override fun refreshFolder(
        remotePath: String,
        accountName: String,
        spaceId: String?,
        isActionSetFolderAvailableOfflineOrSynchronize: Boolean,
    ): List<OCFile> {
        val spaceWebDavUrl = localSpacesDataSource.getWebDavUrlForSpace(spaceId, accountName)

        val fetchFolderResult = remoteFileDataSource.refreshFolder(remotePath, accountName, spaceWebDavUrl).map {
            it.copy(spaceId = spaceId)
        }
        val remoteFolder = fetchFolderResult.first()
        val remoteFolderContent = fetchFolderResult.drop(1)

        val folderContentUpdated = mutableListOf<OCFile>()

        val localFolderByRemotePath: OCFile? =
            localFileDataSource.getFileByRemotePath(remotePath = remoteFolder.remotePath, owner = remoteFolder.owner, spaceId = spaceId)

        if (localFolderByRemotePath == null) {
            folderContentUpdated.addAll(remoteFolderContent.map { it.apply { needsToUpdateThumbnail = !it.isFolder } })
        } else {

            remoteFolder.copyLocalPropertiesFrom(localFolderByRemotePath)

            val localFolderContent = localFileDataSource.getFolderContent(folderId = localFolderByRemotePath.id!!)

            val localFilesMap = localFolderContent.associateBy { localFile -> localFile.remoteId ?: localFile.remotePath }.toMutableMap()

            remoteFolderContent.forEach { remoteChild ->

                val localChildToSync = localFilesMap.remove(remoteChild.remoteId) ?: localFilesMap.remove(remoteChild.remotePath)

                if (localChildToSync == null) {
                    folderContentUpdated.add(
                        remoteChild.apply {
                            parentId = localFolderByRemotePath.id
                            needsToUpdateThumbnail = !remoteChild.isFolder

                            etag = ""
                            availableOfflineStatus =
                                if (remoteFolder.isAvailableOffline) AVAILABLE_OFFLINE_PARENT else NOT_AVAILABLE_OFFLINE

                        })
                } else if (localChildToSync.etag != remoteChild.etag ||
                    localChildToSync.localModificationTimestamp > remoteChild.lastSyncDateForData!! ||
                    isActionSetFolderAvailableOfflineOrSynchronize
                ) {

                    folderContentUpdated.add(
                        remoteChild.apply {
                            copyLocalPropertiesFrom(localChildToSync)

                            etag = localChildToSync.etag
                            needsToUpdateThumbnail =
                                (!remoteChild.isFolder && remoteChild.modificationTimestamp != localChildToSync.modificationTimestamp) || localChildToSync.needsToUpdateThumbnail

                            if (remoteFolder.isAvailableOffline) {
                                availableOfflineStatus = AVAILABLE_OFFLINE_PARENT
                            }

                        })
                }
            }

            localFilesMap.map { it.value }.forEach { ocFile ->
                ocFile.etagInConflict?.let {
                    localFileDataSource.cleanConflict(ocFile.id!!)
                }
                if (ocFile.isFolder) {
                    deleteLocalFolderRecursively(ocFile = ocFile, onlyFromLocalStorage = false)
                } else {
                    deleteLocalFile(ocFile = ocFile, onlyFromLocalStorage = false)
                }
            }
        }

        val anyConflictInThisFolder = folderContentUpdated.any { it.etagInConflict != null }

        if (!anyConflictInThisFolder) {
            remoteFolder.etagInConflict = null
        }

        return localFileDataSource.saveFilesInFolderAndReturnTheFilesThatChanged(
            folder = remoteFolder,
            listOfFiles = folderContentUpdated
        )
    }

    override fun deleteFiles(listOfFilesToDelete: List<OCFile>, removeOnlyLocalCopy: Boolean) {
        val spaceWebDavUrl = localSpacesDataSource.getWebDavUrlForSpace(
            spaceId = listOfFilesToDelete.first().spaceId,
            accountName = listOfFilesToDelete.first().owner,
        )

        listOfFilesToDelete.forEach { ocFile ->
            if (!removeOnlyLocalCopy) {
                try {
                    remoteFileDataSource.deleteFile(
                        remotePath = ocFile.remotePath,
                        accountName = ocFile.owner,
                        spaceWebDavUrl = spaceWebDavUrl,
                    )
                } catch (fileNotFoundException: FileNotFoundException) {
                    Timber.i("File ${ocFile.fileName} was not found in server. Let's remove it from local storage")
                }
            }
            ocFile.etagInConflict?.let {
                localFileDataSource.cleanConflict(ocFile.id!!)
            }
            if (ocFile.isFolder) {
                deleteLocalFolderRecursively(ocFile = ocFile, onlyFromLocalStorage = removeOnlyLocalCopy)
            } else {
                deleteLocalFile(ocFile = ocFile, onlyFromLocalStorage = removeOnlyLocalCopy)
            }
        }
    }

    override fun renameFile(ocFile: OCFile, newName: String) {

        val newRemotePath = localStorageProvider.getExpectedRemotePath(
            remotePath = ocFile.remotePath,
            newName = newName,
            isFolder = ocFile.isFolder
        )

        if (localFileDataSource.getFileByRemotePath(newRemotePath, ocFile.owner, ocFile.spaceId) != null) {
            throw FileAlreadyExistsException()
        }

        val spaceWebDavUrl = localSpacesDataSource.getWebDavUrlForSpace(
            spaceId = ocFile.spaceId,
            accountName = ocFile.owner,
        )

        remoteFileDataSource.renameFile(
            oldName = ocFile.fileName,
            oldRemotePath = ocFile.remotePath,
            newName = newName,
            isFolder = ocFile.isFolder,
            accountName = ocFile.owner,
            spaceWebDavUrl = spaceWebDavUrl,
        )

        localFileDataSource.renameFile(
            fileToRename = ocFile,
            finalRemotePath = newRemotePath,
            finalStoragePath = localStorageProvider.getDefaultSavePathFor(ocFile.owner, newRemotePath, ocFile.spaceId)
        )

        localStorageProvider.moveLocalFile(
            ocFile = ocFile,
            finalStoragePath = localStorageProvider.getDefaultSavePathFor(ocFile.owner, newRemotePath, ocFile.spaceId)
        )
    }

    override fun saveFile(file: OCFile) {
        localFileDataSource.saveFile(file)
    }

    override fun saveConflict(fileId: Long, eTagInConflict: String) {
        localFileDataSource.saveConflict(fileId, eTagInConflict)
    }

    override fun cleanConflict(fileId: Long) {
        localFileDataSource.cleanConflict(fileId)
    }

    override fun disableThumbnailsForFile(fileId: Long) {
        localFileDataSource.disableThumbnailsForFile(fileId)
    }

    override fun updateFileWithNewAvailableOfflineStatus(ocFile: OCFile, newAvailableOfflineStatus: AvailableOfflineStatus) {
        localFileDataSource.updateAvailableOfflineStatusForFile(ocFile, newAvailableOfflineStatus)
    }

    override fun updateFileWithLastUsage(fileId: Long, lastUsage: Long?) {
        localFileDataSource.updateFileWithLastUsage(fileId, lastUsage)
    }

    override fun updateDownloadedFilesStorageDirectoryInStoragePath(oldDirectory: String, newDirectory: String) {
        localFileDataSource.updateDownloadedFilesStorageDirectoryInStoragePath(oldDirectory, newDirectory)
    }

    override fun saveUploadWorkerUuid(fileId: Long, workerUuid: UUID) {
        TODO("Not yet implemented")
    }

    override fun saveDownloadWorkerUuid(fileId: Long, workerUuid: UUID) {
        localFileDataSource.saveDownloadWorkerUuid(fileId, workerUuid)
    }

    override fun cleanWorkersUuid(fileId: Long) {
        localFileDataSource.cleanWorkersUuid(fileId)
    }

    private fun deleteLocalFolderRecursively(ocFile: OCFile, onlyFromLocalStorage: Boolean) {
        val folderContent = localFileDataSource.getFolderContent(ocFile.id!!)

        folderContent.forEach { file ->
            if (!(onlyFromLocalStorage && file.isAvailableOffline)) { // The condition will not be met when onlyFromLocalStorage is true and the file is of type available offline
                if (file.isFolder) {
                    deleteLocalFolderRecursively(ocFile = file, onlyFromLocalStorage = onlyFromLocalStorage)
                } else {
                    deleteLocalFile(ocFile = file, onlyFromLocalStorage = onlyFromLocalStorage)
                }
            }
        }

        deleteLocalFolderIfItHasNoFilesInside(ocFolder = ocFile, onlyFromLocalStorage = onlyFromLocalStorage)
    }

    private fun deleteLocalFolderIfItHasNoFilesInside(ocFolder: OCFile, onlyFromLocalStorage: Boolean) {
        localStorageProvider.deleteLocalFolderIfItHasNoFilesInside(ocFolder = ocFolder)
        deleteOrResetFileFromDatabase(ocFolder, onlyFromLocalStorage)
    }

    private fun deleteLocalFile(ocFile: OCFile, onlyFromLocalStorage: Boolean) {
        localStorageProvider.deleteLocalFile(ocFile)
        deleteOrResetFileFromDatabase(ocFile, onlyFromLocalStorage)
    }

    private fun deleteOrResetFileFromDatabase(ocFile: OCFile, onlyFromLocalStorage: Boolean) {
        if (onlyFromLocalStorage) {
            localFileDataSource.saveFile(ocFile.copy(storagePath = null, etagInConflict = null, lastUsage = null, etag = null))
        } else {
            localFileDataSource.deleteFile(ocFile.id!!)
        }
    }
}
