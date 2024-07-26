

package com.owncloud.android.data.providers

import android.accounts.Account
import android.annotation.SuppressLint
import android.net.Uri
import com.owncloud.android.data.extensions.moveRecursively
import com.owncloud.android.domain.files.model.OCFile
import com.owncloud.android.domain.transfers.model.OCTransfer
import timber.log.Timber
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

sealed class LocalStorageProvider(private val rootFolderName: String) {

    abstract fun getPrimaryStorageDirectory(): File


    fun getRootFolderPath(): String = getPrimaryStorageDirectory().absolutePath + File.separator + rootFolderName


    private fun getAccountDirectoryPath(
        accountName: String
    ): String = getRootFolderPath() + File.separator + getEncodedAccountName(accountName)


    fun getDefaultSavePathFor(
        accountName: String,
        remotePath: String,
        spaceId: String?,
    ): String {
        return if (spaceId != null) {
            getAccountDirectoryPath(accountName) + File.separator + spaceId + File.separator + remotePath
        } else {
            getAccountDirectoryPath(accountName) + remotePath
        }
    }


    fun getExpectedRemotePath(remotePath: String, newName: String, isFolder: Boolean): String {
        var parent = (File(remotePath)).parent ?: throw IllegalArgumentException()
        parent = if (parent.endsWith(File.separator)) parent else parent + File.separator
        var newRemotePath = parent + newName
        if (isFolder) {
            newRemotePath += File.separator
        }
        return newRemotePath
    }


    fun getTemporalPath(
        accountName: String?,
        spaceId: String? = null,
    ): String {
        val temporalPathWithoutSpace =
            getRootFolderPath() + File.separator + TEMPORAL_FOLDER_NAME + File.separator + getEncodedAccountName(accountName)

        return if (spaceId != null) {
            temporalPathWithoutSpace + File.separator + spaceId
        } else {
            temporalPathWithoutSpace
        }
    }

    fun getLogsPath(): String = getRootFolderPath() + File.separator + LOGS_FOLDER_NAME + File.separator


    @SuppressLint("UsableSpace")
    fun getUsableSpace(): Long = getPrimaryStorageDirectory().usableSpace


    private fun getDanglingAccountDirs(remainingAccounts: Array<Account>): List<File> {
        val rootFolder = File(getRootFolderPath())
        val danglingDirs = mutableListOf<File>()
        rootFolder.listFiles()?.forEach { dir ->
            var dirIsOk = false
            if (dir.name.equals(TEMPORAL_FOLDER_NAME) || dir.name.equals(LOGS_FOLDER_NAME)) {
                dirIsOk = true
            } else {
                remainingAccounts.forEach { account ->
                    if (dir.name.equals(getEncodedAccountName(account.name))) {
                        dirIsOk = true
                    }
                }
            }
            if (!dirIsOk) {
                danglingDirs.add(dir)
            }
        }
        return danglingDirs
    }


    open fun deleteUnusedUserDirs(remainingAccounts: Array<Account>) {
        val danglingDirs = getDanglingAccountDirs(remainingAccounts)
        danglingDirs.forEach { dd ->
            dd.deleteRecursively()
        }
    }


    private fun getEncodedAccountName(accountName: String?): String = Uri.encode(accountName, "@")

    fun moveLegacyToScopedStorage() {
        val timeInMillis = measureTimeMillis {
            moveFileOrFolderToScopedStorage(retrieveRootLegacyStorage())
        }
        Timber.d("MIGRATED FILES IN ${TimeUnit.SECONDS.convert(timeInMillis, TimeUnit.MILLISECONDS)} seconds")
    }

    private fun retrieveRootLegacyStorage(): File {
        val legacyStorageProvider = LegacyStorageProvider(rootFolderName)
        val rootLegacyStorage = File(legacyStorageProvider.getRootFolderPath())

        val legacyStorageUsedBytes = sizeOfDirectory(rootLegacyStorage)
        Timber.d(
            "Root ${rootLegacyStorage.absolutePath} has ${rootLegacyStorage.listFiles()?.size} files and its size is $legacyStorageUsedBytes Bytes"
        )

        return rootLegacyStorage
    }

    private fun moveFileOrFolderToScopedStorage(rootLegacyDirectory: File) {
        Timber.d("Let's move ${rootLegacyDirectory.absolutePath} to scoped storage")
        rootLegacyDirectory.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                file.moveRecursively(File(getRootFolderPath(), file.name), overwrite = true)
            }
        }
        rootLegacyDirectory.deleteRecursively()
    }

    fun sizeOfDirectory(dir: File): Long {
        if (dir.exists()) {
            var result: Long = 0
            val fileList = dir.listFiles() ?: arrayOf()
            fileList.forEach { file ->

                result += if (file.isDirectory) {
                    sizeOfDirectory(file)
                } else {

                    file.length()
                }
            }
            return result // return the file size
        }
        return 0

    }


    fun deleteLocalFile(ocFile: OCFile): Boolean {
        val safeStoragePath = ocFile.getStoragePathOrExpectedPathForFile()
        val fileToDelete = File(safeStoragePath)

        if (!fileToDelete.exists()) {
            return true
        }

        return fileToDelete.deleteRecursively()
    }

    fun deleteLocalFolderIfItHasNoFilesInside(ocFolder: OCFile) {
        val safeStoragePath = ocFolder.getStoragePathOrExpectedPathForFile()
        val folder = File(safeStoragePath)

        val filesInFolder = folder.listFiles()
        if (filesInFolder.isNullOrEmpty()) {
            folder.delete()
        }
    }

    fun moveLocalFile(ocFile: OCFile, finalStoragePath: String) {
        val safeStoragePath = ocFile.getStoragePathOrExpectedPathForFile()
        val fileToMove = File(safeStoragePath)

        if (!fileToMove.exists()) {
            return
        }
        val targetFile = File(finalStoragePath)
        val targetFolder = targetFile.parentFile
        if (targetFolder != null && !targetFolder.exists()) {
            targetFolder.mkdirs()
        }
        fileToMove.renameTo(targetFile)
    }

    fun clearUnrelatedTemporalFiles(uploads: List<OCTransfer>, accountsNames: List<String>) {
        accountsNames.forEach { accountName ->
            val temporalFolderForAccount = File(getTemporalPath(accountName))
            cleanTemporalRecursively(temporalFolderForAccount) { temporalFile ->
                if (!uploads.map { it.localPath }.contains(temporalFile.absolutePath)) {
                    temporalFile.delete()
                }
            }
        }
    }

    private fun cleanTemporalRecursively(temporalFolder: File, deleteFileInCaseItIsNotNeeded: (file: File) -> Unit) {
        temporalFolder.listFiles()?.forEach { temporalFile ->
            if (temporalFile.isDirectory) {
                cleanTemporalRecursively(temporalFile, deleteFileInCaseItIsNotNeeded)
            } else {
                deleteFileInCaseItIsNotNeeded(temporalFile)
            }

        }
    }

    fun removeLocalStorageForAccount(accountName: String) {
        val mainFolderForAccount = File(getAccountDirectoryPath(accountName))
        val temporalFolderForAccount = File(getTemporalPath(accountName))
        mainFolderForAccount.deleteRecursively()
        temporalFolderForAccount.deleteRecursively()
    }

    fun deleteCacheIfNeeded(transfer: OCTransfer) {
        val cacheDir = getTemporalPath(transfer.accountName)
        if (transfer.localPath.startsWith(cacheDir)) {
            val cacheFile = File(transfer.localPath)
            cacheFile.delete()
        }
    }


    private fun OCFile.getStoragePathOrExpectedPathForFile() =
        storagePath.takeUnless { it.isNullOrBlank() } ?: getDefaultSavePathFor(
            accountName = owner,
            remotePath = remotePath,
            spaceId = spaceId,
        )

    companion object {
        private const val LOGS_FOLDER_NAME = "logs"
        private const val TEMPORAL_FOLDER_NAME = "tmp"
    }
}
