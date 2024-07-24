

package com.owncloud.android.datamodel

import android.accounts.Account
import com.owncloud.android.domain.files.model.OCFile
import com.owncloud.android.domain.files.model.OCFile.Companion.ROOT_PATH
import com.owncloud.android.domain.files.usecases.GetFileByIdUseCase
import com.owncloud.android.domain.files.usecases.GetFileByRemotePathUseCase
import com.owncloud.android.domain.files.usecases.GetFolderContentUseCase
import com.owncloud.android.domain.files.usecases.GetFolderImagesUseCase
import com.owncloud.android.domain.files.usecases.GetPersonalRootFolderForAccountUseCase
import com.owncloud.android.domain.files.usecases.GetSharesRootFolderForAccount
import com.owncloud.android.providers.CoroutinesDispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class FileDataStorageManager(
    val account: Account,
) : KoinComponent {

    fun getFileByPath(remotePath: String, spaceId: String? = null): OCFile? =
        if (remotePath == ROOT_PATH && spaceId == null) {
            getRootPersonalFolder()
        } else {
            getFileByPathAndAccount(remotePath, account.name, spaceId)
        }

    private fun getFileByPathAndAccount(remotePath: String, accountName: String, spaceId: String? = null): OCFile? = runBlocking(CoroutinesDispatcherProvider().io) {
        val getFileByRemotePathUseCase: GetFileByRemotePathUseCase by inject()

        val result = withContext(CoroutineScope(CoroutinesDispatcherProvider().io).coroutineContext) {
            getFileByRemotePathUseCase(GetFileByRemotePathUseCase.Params(accountName, remotePath, spaceId))
        }.getDataOrNull()
        result
    }

    fun getRootPersonalFolder() = runBlocking(CoroutinesDispatcherProvider().io) {
        val getPersonalRootFolderForAccountUseCase: GetPersonalRootFolderForAccountUseCase by inject()

        val result = withContext(CoroutineScope(CoroutinesDispatcherProvider().io).coroutineContext) {
            getPersonalRootFolderForAccountUseCase(GetPersonalRootFolderForAccountUseCase.Params(account.name))
        }
        result
    }

    fun getRootSharesFolder() = runBlocking(CoroutinesDispatcherProvider().io) {
        val getSharesRootFolderForAccount: GetSharesRootFolderForAccount by inject()

        val result = withContext(CoroutineScope(CoroutinesDispatcherProvider().io).coroutineContext) {
            getSharesRootFolderForAccount(GetSharesRootFolderForAccount.Params(account.name))
        }
        result
    }

    // TODO: New_arch: Remove this and call usecase inside FilesViewModel
    fun getFileById(id: Long): OCFile? = runBlocking(CoroutinesDispatcherProvider().io) {
        val getFileByIdUseCase: GetFileByIdUseCase by inject()

        val result = withContext(CoroutineScope(CoroutinesDispatcherProvider().io).coroutineContext) {
            getFileByIdUseCase(GetFileByIdUseCase.Params(id))
        }.getDataOrNull()
        result
    }

    fun fileExists(path: String): Boolean = getFileByPath(path) != null

    fun getFolderContent(f: OCFile?): List<OCFile> {
        return if (f != null && f.isFolder && f.id != -1L) {
            // TODO: Remove !!
            getFolderContent(f.id!!)
        } else {
            listOf()
        }
    }

    // TODO: New_arch: Remove this and call usecase inside FilesViewModel
    fun getFolderImages(folder: OCFile?): List<OCFile> = runBlocking(CoroutinesDispatcherProvider().io) {
        val getFolderImagesUseCase: GetFolderImagesUseCase by inject()

        val result = withContext(CoroutineScope(CoroutinesDispatcherProvider().io).coroutineContext) {
            // TODO: Remove !!
            getFolderImagesUseCase(GetFolderImagesUseCase.Params(folderId = folder!!.id!!))
        }.getDataOrNull()
        result ?: listOf()
    }

    // TODO: New_arch: Remove this and call usecase inside FilesViewModel
    private fun getFolderContent(parentId: Long): List<OCFile> = runBlocking(CoroutinesDispatcherProvider().io) {
        val getFolderContentUseCase: GetFolderContentUseCase by inject()

        val result = withContext(CoroutineScope(CoroutinesDispatcherProvider().io).coroutineContext) {
            getFolderContentUseCase(GetFolderContentUseCase.Params(parentId))
        }.getDataOrNull()
        result ?: listOf()
    }
}
