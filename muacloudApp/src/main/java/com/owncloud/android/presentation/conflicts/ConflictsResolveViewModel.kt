

package com.owncloud.android.presentation.conflicts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.owncloud.android.domain.files.model.OCFile
import com.owncloud.android.domain.files.usecases.GetFileByIdAsStreamUseCase
import com.owncloud.android.providers.CoroutinesDispatcherProvider
import com.owncloud.android.usecases.transfers.downloads.DownloadFileUseCase
import com.owncloud.android.usecases.transfers.uploads.UploadFileInConflictUseCase
import com.owncloud.android.usecases.transfers.uploads.UploadFilesFromSystemUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ConflictsResolveViewModel(
    private val downloadFileUseCase: DownloadFileUseCase,
    private val uploadFileInConflictUseCase: UploadFileInConflictUseCase,
    private val uploadFilesFromSystemUseCase: UploadFilesFromSystemUseCase,
    getFileByIdAsStreamUseCase: GetFileByIdAsStreamUseCase,
    private val coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    ocFile: OCFile,
) : ViewModel() {

    val currentFile: StateFlow<OCFile?> =
        getFileByIdAsStreamUseCase(GetFileByIdAsStreamUseCase.Params(ocFile.id!!))
            .stateIn(
                viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = ocFile
            )

    fun downloadFile() {
        val fileToDownload = currentFile.value ?: return
        viewModelScope.launch(coroutinesDispatcherProvider.io) {
            downloadFileUseCase(
                DownloadFileUseCase.Params(
                    accountName = fileToDownload.owner,
                    file = fileToDownload
                )
            )
        }
    }

    fun uploadFileInConflict() {
        val fileToUpload = currentFile.value ?: return
        viewModelScope.launch(coroutinesDispatcherProvider.io) {
            uploadFileInConflictUseCase(
                UploadFileInConflictUseCase.Params(
                    accountName = fileToUpload.owner,
                    localPath = fileToUpload.storagePath!!,
                    uploadFolderPath = fileToUpload.getParentRemotePath(),
                    spaceId = fileToUpload.spaceId,
                )
            )
        }
    }

    fun uploadFileFromSystem() {
        val fileToUpload = currentFile.value ?: return
        viewModelScope.launch(coroutinesDispatcherProvider.io) {
            uploadFilesFromSystemUseCase(
                UploadFilesFromSystemUseCase.Params(
                    accountName = fileToUpload.owner,
                    listOfLocalPaths = listOf(fileToUpload.storagePath!!),
                    uploadFolderPath = fileToUpload.getParentRemotePath(),
                    spaceId = fileToUpload.spaceId,
                )
            )
        }
    }
}
