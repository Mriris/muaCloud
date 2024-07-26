

package com.owncloud.android.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.owncloud.android.domain.capabilities.usecases.GetStoredCapabilitiesUseCase
import com.owncloud.android.domain.files.model.OCFile
import com.owncloud.android.domain.files.model.OCFile.Companion.ROOT_PATH
import com.owncloud.android.domain.files.usecases.GetFileByRemotePathUseCase
import com.owncloud.android.domain.spaces.usecases.GetPersonalAndProjectSpacesForAccountUseCase
import com.owncloud.android.domain.spaces.usecases.RefreshSpacesFromServerAsyncUseCase
import com.owncloud.android.presentation.authentication.AccountUtils
import com.owncloud.android.usecases.synchronization.SynchronizeFolderUseCase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class AccountDiscoveryWorker(
    private val appContext: Context,
    private val workerParameters: WorkerParameters
) : CoroutineWorker(
    appContext,
    workerParameters
), KoinComponent {

    private val getStoredCapabilitiesUseCase: GetStoredCapabilitiesUseCase by inject()
    private val refreshSpacesFromServerAsyncUseCase: RefreshSpacesFromServerAsyncUseCase by inject()
    private val getPersonalAndProjectSpacesForAccountUseCase: GetPersonalAndProjectSpacesForAccountUseCase by inject()
    private val getFileByRemotePathUseCase: GetFileByRemotePathUseCase by inject()
    private val synchronizeFolderUseCase: SynchronizeFolderUseCase by inject()

    override suspend fun doWork(): Result {
        val accountName = workerParameters.inputData.getString(KEY_PARAM_DISCOVERY_ACCOUNT)
        val account = AccountUtils.getOwnCloudAccountByName(appContext, accountName)
        Timber.d("Account Discovery for account: $accountName and accountName: ${account.name}")

        if (accountName.isNullOrBlank() || account == null) return Result.failure()

        val capabilities = getStoredCapabilitiesUseCase(GetStoredCapabilitiesUseCase.Params(accountName))

        val spacesAvailableForAccount = AccountUtils.isSpacesFeatureAllowedForAccount(appContext, account, capabilities)

        if (!spacesAvailableForAccount) {
            val rootLegacyFolder = getFileByRemotePathUseCase(GetFileByRemotePathUseCase.Params(accountName, ROOT_PATH, null)).getDataOrNull()
            rootLegacyFolder?.let {
                discoverRootFolder(it)
            }
        } else {
            val spacesRootFoldersToDiscover = mutableListOf<OCFile>()

            refreshSpacesFromServerAsyncUseCase(RefreshSpacesFromServerAsyncUseCase.Params(accountName))
            val spaces = getPersonalAndProjectSpacesForAccountUseCase(GetPersonalAndProjectSpacesForAccountUseCase.Params(accountName))

            val personalSpace = spaces.firstOrNull { it.isPersonal }
            personalSpace?.let { space ->
                val rootFolderForSpace =
                    getFileByRemotePathUseCase(GetFileByRemotePathUseCase.Params(accountName, ROOT_PATH, space.root.id)).getDataOrNull()
                rootFolderForSpace?.let {
                    discoverRootFolder(it)
                }
            }

            val spacesWithoutPersonal = spaces.filterNot { it.isPersonal }
            spacesWithoutPersonal.forEach { space ->

                val rootFolderForSpace =
                    getFileByRemotePathUseCase(GetFileByRemotePathUseCase.Params(accountName, ROOT_PATH, space.root.id)).getDataOrNull()
                rootFolderForSpace?.let {
                    spacesRootFoldersToDiscover.add(it)
                }
            }
            spacesRootFoldersToDiscover.forEach {
                discoverRootFolder(it)
            }
        }

        return Result.success()
    }

    private fun discoverRootFolder(folder: OCFile) {
        synchronizeFolderUseCase(
            SynchronizeFolderUseCase.Params(
                accountName = folder.owner,
                remotePath = folder.remotePath,
                spaceId = folder.spaceId,
                syncMode = SynchronizeFolderUseCase.SyncFolderMode.REFRESH_FOLDER_RECURSIVELY
            )
        )
    }

    companion object {
        const val KEY_PARAM_DISCOVERY_ACCOUNT = "KEY_PARAM_DISCOVERY_ACCOUNT"
    }
}
