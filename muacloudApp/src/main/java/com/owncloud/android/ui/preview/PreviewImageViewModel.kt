

package com.owncloud.android.ui.preview

import android.accounts.Account
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import com.owncloud.android.R
import com.owncloud.android.domain.files.model.FileMenuOption
import com.owncloud.android.domain.files.model.OCFile
import com.owncloud.android.domain.files.usecases.GetFileByIdUseCase
import com.owncloud.android.providers.ContextProvider
import com.owncloud.android.providers.CoroutinesDispatcherProvider
import com.owncloud.android.usecases.files.FilterFileMenuOptionsUseCase
import com.owncloud.android.usecases.transfers.downloads.GetLiveDataForFinishedDownloadsFromAccountUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PreviewImageViewModel(
    private val getFileByIdUseCase: GetFileByIdUseCase,
    private val getLiveDataForFinishedDownloadsFromAccountUseCase: GetLiveDataForFinishedDownloadsFromAccountUseCase,
    private val filterFileMenuOptionsUseCase: FilterFileMenuOptionsUseCase,
    private val contextProvider: ContextProvider,
    private val coroutinesDispatcherProvider: CoroutinesDispatcherProvider
) : ViewModel() {

    private val _downloads = MediatorLiveData<List<Pair<OCFile, WorkInfo>>>()
    val downloads: LiveData<List<Pair<OCFile, WorkInfo>>> = _downloads

    private val _menuOptions: MutableStateFlow<List<FileMenuOption>> = MutableStateFlow(emptyList())
    val menuOptions: StateFlow<List<FileMenuOption>> = _menuOptions

    fun startListeningToDownloadsFromAccount(account: Account) {
        _downloads.addSource(
            getLiveDataForFinishedDownloadsFromAccountUseCase(GetLiveDataForFinishedDownloadsFromAccountUseCase.Params(account))
        ) { listOfWorkInfo ->
            viewModelScope.launch(coroutinesDispatcherProvider.io) {
                val finalList = getListOfPairs(listOfWorkInfo)
                _downloads.postValue(finalList)
            }
        }
    }

    fun filterMenuOptions(file: OCFile, accountName: String) {
        val shareViaLinkAllowed = contextProvider.getBoolean(R.bool.share_via_link_feature)
        val shareWithUsersAllowed = contextProvider.getBoolean(R.bool.share_with_users_feature)
        val sendAllowed = contextProvider.getString(R.string.send_files_to_other_apps).equals("on", ignoreCase = true)
        viewModelScope.launch(coroutinesDispatcherProvider.io) {
            val result = filterFileMenuOptionsUseCase(
                FilterFileMenuOptionsUseCase.Params(
                    files = listOf(file),
                    accountName = accountName,
                    isAnyFileVideoPreviewing = false,
                    displaySelectAll = false,
                    displaySelectInverse = false,
                    onlyAvailableOfflineFiles = false,
                    onlySharedByLinkFiles = false,
                    shareViaLinkAllowed = shareViaLinkAllowed,
                    shareWithUsersAllowed = shareWithUsersAllowed,
                    sendAllowed = sendAllowed,
                )
            )
            result.apply {
                remove(FileMenuOption.RENAME)
                remove(FileMenuOption.MOVE)
                remove(FileMenuOption.COPY)
                remove(FileMenuOption.SYNC)
            }
            _menuOptions.update { result }
        }
    }


    private fun getListOfPairs(
        listOfWorkInfo: List<WorkInfo>
    ): List<Pair<OCFile, WorkInfo>> {
        val finalList = mutableListOf<Pair<OCFile, WorkInfo>>()

        listOfWorkInfo.forEach { workInfo ->
            val id: Long = workInfo.tags.first { it.toLongOrNull() != null }.toLong()
            val useCaseResult = getFileByIdUseCase(GetFileByIdUseCase.Params(fileId = id))
            val file = useCaseResult.getDataOrNull()
            if (file != null) {
                finalList.add(Pair(file, workInfo))
            }
        }
        return finalList
    }
}
