
package com.owncloud.android.usecases.synchronization

import com.owncloud.android.domain.BaseUseCaseWithResult
import com.owncloud.android.domain.extensions.isOneOf
import com.owncloud.android.domain.files.FileRepository
import com.owncloud.android.domain.files.model.OCFile
import com.owncloud.android.usecases.synchronization.SynchronizeFolderUseCase.SyncFolderMode.REFRESH_FOLDER_RECURSIVELY
import com.owncloud.android.usecases.synchronization.SynchronizeFolderUseCase.SyncFolderMode.SYNC_CONTENTS
import com.owncloud.android.usecases.synchronization.SynchronizeFolderUseCase.SyncFolderMode.SYNC_FOLDER_RECURSIVELY

class SynchronizeFolderUseCase(
    private val synchronizeFileUseCase: SynchronizeFileUseCase,
    private val fileRepository: FileRepository,
) : BaseUseCaseWithResult<Unit, SynchronizeFolderUseCase.Params>() {

    override fun run(params: Params) {
        val remotePath = params.remotePath
        val accountName = params.accountName

        val folderContent = fileRepository.refreshFolder(
            remotePath = remotePath,
            accountName = accountName,
            spaceId = params.spaceId,
            isActionSetFolderAvailableOfflineOrSynchronize = params.isActionSetFolderAvailableOfflineOrSynchronize,
        )

        folderContent.forEach { ocFile ->
            if (ocFile.isFolder) {
                if (shouldSyncFolder(params.syncMode, ocFile)) {
                    SynchronizeFolderUseCase(synchronizeFileUseCase, fileRepository)(
                        Params(
                            remotePath = ocFile.remotePath,
                            accountName = accountName,
                            spaceId = ocFile.spaceId,
                            syncMode = params.syncMode,
                            isActionSetFolderAvailableOfflineOrSynchronize = params.isActionSetFolderAvailableOfflineOrSynchronize,
                        )
                    )
                }
            } else if (shouldSyncFile(params.syncMode, ocFile)) {
                synchronizeFileUseCase(
                    SynchronizeFileUseCase.Params(
                        fileToSynchronize = ocFile,
                    )
                )
            }
        }
    }

    private fun shouldSyncFolder(syncMode: SyncFolderMode, ocFolder: OCFile) =
        syncMode.isOneOf(REFRESH_FOLDER_RECURSIVELY, SYNC_FOLDER_RECURSIVELY) || syncMode == SYNC_CONTENTS && ocFolder.isAvailableOffline

    private fun shouldSyncFile(syncMode: SyncFolderMode, ocFile: OCFile) =
        syncMode == SYNC_FOLDER_RECURSIVELY || (syncMode == SYNC_CONTENTS && (ocFile.isAvailableLocally || ocFile.isAvailableOffline))

    data class Params(
        val remotePath: String,
        val accountName: String,
        val spaceId: String? = null,
        val syncMode: SyncFolderMode,
        val isActionSetFolderAvailableOfflineOrSynchronize: Boolean = false,
    )


    enum class SyncFolderMode {
        REFRESH_FOLDER, REFRESH_FOLDER_RECURSIVELY, SYNC_CONTENTS, SYNC_FOLDER_RECURSIVELY;
    }
}
