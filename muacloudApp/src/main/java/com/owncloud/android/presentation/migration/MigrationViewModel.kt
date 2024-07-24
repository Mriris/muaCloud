

package com.owncloud.android.presentation.migration

import android.os.Environment
import android.os.StatFs
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.owncloud.android.data.providers.SharedPreferencesProvider
import com.owncloud.android.data.providers.LegacyStorageProvider
import com.owncloud.android.data.providers.LocalStorageProvider
import com.owncloud.android.domain.files.usecases.UpdateAlreadyDownloadedFilesPathUseCase
import com.owncloud.android.domain.transfers.usecases.GetAllTransfersUseCase
import com.owncloud.android.domain.transfers.usecases.UpdatePendingUploadsPathUseCase
import com.owncloud.android.domain.utils.Event
import com.owncloud.android.presentation.migration.StorageMigrationActivity.Companion.PREFERENCE_ALREADY_MIGRATED_TO_SCOPED_STORAGE
import com.owncloud.android.providers.AccountProvider
import com.owncloud.android.providers.CoroutinesDispatcherProvider
import kotlinx.coroutines.launch
import java.io.File


class MigrationViewModel(
    rootFolder: String,
    private val localStorageProvider: LocalStorageProvider,
    private val preferencesProvider: SharedPreferencesProvider,
    private val accountProvider: AccountProvider,
    private val updatePendingUploadsPathUseCase: UpdatePendingUploadsPathUseCase,
    private val updateAlreadyDownloadedFilesPathUseCase: UpdateAlreadyDownloadedFilesPathUseCase,
    private val getAllTransfersUseCase: GetAllTransfersUseCase,
    private val coroutineDispatcherProvider: CoroutinesDispatcherProvider,
) : ViewModel() {

    private val _migrationState = MediatorLiveData<Event<MigrationState>>()
    val migrationState: LiveData<Event<MigrationState>> = _migrationState

    private val legacyStorageDirectoryPath = LegacyStorageProvider(rootFolder).getRootFolderPath()

    init {
        _migrationState.postValue(Event(MigrationState.MigrationIntroState))
    }

    private fun getLegacyStorageSizeInBytes(): Long {
        val legacyStorageDirectory = File(legacyStorageDirectoryPath)
        return localStorageProvider.sizeOfDirectory(legacyStorageDirectory)
    }

    fun moveLegacyStorageToScopedStorage() {
        viewModelScope.launch(coroutineDispatcherProvider.io) {
            localStorageProvider.moveLegacyToScopedStorage()
            updatePendingUploadsPath()
            updateAlreadyDownloadedFilesPath()
            moveToNextState()
        }
    }

    private fun saveAlreadyMigratedPreference() {
        preferencesProvider.putBoolean(key = PREFERENCE_ALREADY_MIGRATED_TO_SCOPED_STORAGE, value = true)
    }

    private fun updatePendingUploadsPath() {
        updatePendingUploadsPathUseCase(
            UpdatePendingUploadsPathUseCase.Params(
                oldDirectory = legacyStorageDirectoryPath,
                newDirectory = localStorageProvider.getRootFolderPath()
            )
        )
        val uploads = getAllTransfersUseCase(Unit)
        val accountsNames = mutableListOf<String>()
        accountProvider.getLoggedAccounts().forEach { account ->
            accountsNames.add(localStorageProvider.getTemporalPath(account.name))
        }
        localStorageProvider.clearUnrelatedTemporalFiles(uploads, accountsNames)
    }

    private fun updateAlreadyDownloadedFilesPath() {
        updateAlreadyDownloadedFilesPathUseCase(
            UpdateAlreadyDownloadedFilesPathUseCase.Params(
                oldDirectory = legacyStorageDirectoryPath,
                newDirectory = localStorageProvider.getRootFolderPath()
            )
        )
    }

    fun moveToNextState() {

        val nextState: MigrationState = when (_migrationState.value?.peekContent()) {
            is MigrationState.MigrationIntroState -> MigrationState.MigrationChoiceState(
                legacyStorageSpaceInBytes = getLegacyStorageSizeInBytes()
            )
            is MigrationState.MigrationChoiceState -> MigrationState.MigrationProgressState
            is MigrationState.MigrationProgressState -> MigrationState.MigrationCompletedState
            is MigrationState.MigrationCompletedState -> MigrationState.MigrationCompletedState
            null -> MigrationState.MigrationIntroState
        }

        if (nextState is MigrationState.MigrationCompletedState) {
            saveAlreadyMigratedPreference()
        }

        _migrationState.postValue(Event(nextState))
    }

    fun isThereEnoughSpaceInDevice(): Boolean {
        val stat = StatFs(Environment.getDataDirectory().path)
        val availableBytes = stat.availableBlocksLong * stat.blockSizeLong
        return availableBytes > getLegacyStorageSizeInBytes()
    }
}
