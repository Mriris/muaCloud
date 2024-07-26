

package com.owncloud.android.usecases.synchronization

import com.owncloud.android.domain.BaseUseCaseWithResult
import com.owncloud.android.domain.exceptions.FileNotFoundException
import com.owncloud.android.domain.files.FileRepository
import com.owncloud.android.domain.files.model.OCFile
import com.owncloud.android.domain.files.usecases.SaveConflictUseCase
import com.owncloud.android.usecases.transfers.downloads.DownloadFileUseCase
import com.owncloud.android.usecases.transfers.uploads.UploadFileInConflictUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import timber.log.Timber
import java.util.UUID

class SynchronizeFileUseCase(
    private val downloadFileUseCase: DownloadFileUseCase,
    private val uploadFileInConflictUseCase: UploadFileInConflictUseCase,
    private val saveConflictUseCase: SaveConflictUseCase,
    private val fileRepository: FileRepository,
) : BaseUseCaseWithResult<SynchronizeFileUseCase.SyncType, SynchronizeFileUseCase.Params>() {

    override fun run(params: Params): SyncType {
        val fileToSynchronize = params.fileToSynchronize
        val accountName: String = fileToSynchronize.owner

        CoroutineScope(Dispatchers.IO).run {

            val serverFile = try {
                fileRepository.readFile(
                    remotePath = fileToSynchronize.remotePath,
                    accountName = fileToSynchronize.owner,
                    spaceId = fileToSynchronize.spaceId
                )
            } catch (exception: FileNotFoundException) {

                val localFile = fileToSynchronize.id?.let { fileRepository.getFileById(it) }



                if (localFile != null && (localFile.remotePath == fileToSynchronize.remotePath && localFile.spaceId == fileToSynchronize.spaceId)) {
                    fileRepository.deleteFiles(listOf(fileToSynchronize), true)
                }
                return SyncType.FileNotFound
            }

            if (!fileToSynchronize.isAvailableLocally) {
                Timber.i("File ${fileToSynchronize.fileName} is not downloaded. Let's download it")
                val uuid = requestForDownload(accountName = accountName, ocFile = fileToSynchronize)
                return SyncType.DownloadEnqueued(uuid)
            }

            val changedLocally = fileToSynchronize.localModificationTimestamp > fileToSynchronize.lastSyncDateForData!!
            Timber.i("Local file modification timestamp :${fileToSynchronize.localModificationTimestamp} and last sync date for data :${fileToSynchronize.lastSyncDateForData}")
            Timber.i("So it has changed locally: $changedLocally")

            val changedRemotely = serverFile.etag != fileToSynchronize.etag
            Timber.i("Local etag :${fileToSynchronize.etag} and remote etag :${serverFile.etag}")
            Timber.i("So it has changed remotely: $changedRemotely")

            if (changedLocally && changedRemotely) {

                Timber.i("File ${fileToSynchronize.fileName} has changed locally and remotely. We got a conflict with etag: ${serverFile.etag}")
                if (fileToSynchronize.etagInConflict == null) {
                    saveConflictUseCase(
                        SaveConflictUseCase.Params(
                            fileId = fileToSynchronize.id!!,
                            eTagInConflict = serverFile.etag!!
                        )
                    )
                }
                return SyncType.ConflictDetected(serverFile.etag!!)
            } else if (changedRemotely) {

                Timber.i("File ${fileToSynchronize.fileName} has changed remotely. Let's download the new version")
                val uuid = requestForDownload(accountName, fileToSynchronize)
                return SyncType.DownloadEnqueued(uuid)
            } else if (changedLocally) {

                Timber.i("File ${fileToSynchronize.fileName} has changed locally. Let's upload the new version")
                val uuid = requestForUpload(accountName, fileToSynchronize, fileToSynchronize.etag!!)
                return SyncType.UploadEnqueued(uuid)
            } else {

                Timber.i("File ${fileToSynchronize.fileName} is already synchronized. Nothing to do here")
                return SyncType.AlreadySynchronized
            }
        }
    }

    private fun requestForDownload(accountName: String, ocFile: OCFile): UUID? {
        return downloadFileUseCase(
            DownloadFileUseCase.Params(
                accountName = accountName,
                file = ocFile
            )
        )
    }

    private fun requestForUpload(accountName: String, ocFile: OCFile, etagInConflict: String): UUID? {
        return uploadFileInConflictUseCase(
            UploadFileInConflictUseCase.Params(
                accountName = accountName,
                localPath = ocFile.storagePath!!,
                uploadFolderPath = ocFile.getParentRemotePath(),
                spaceId = ocFile.spaceId,
            )
        )
    }

    data class Params(
        val fileToSynchronize: OCFile,
    )

    sealed interface SyncType {
        object FileNotFound : SyncType
        data class ConflictDetected(val etagInConflict: String) : SyncType
        data class DownloadEnqueued(val workerId: UUID?) : SyncType
        data class UploadEnqueued(val workerId: UUID?) : SyncType
        object AlreadySynchronized : SyncType
    }
}
