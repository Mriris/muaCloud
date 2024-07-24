

package com.owncloud.android.presentation.accounts

import android.accounts.Account
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.owncloud.android.domain.camerauploads.model.CameraUploadsConfiguration
import com.owncloud.android.domain.camerauploads.usecases.GetCameraUploadsConfigurationUseCase
import com.owncloud.android.domain.utils.Event
import com.owncloud.android.extensions.ViewModelExt.runUseCaseWithResult
import com.owncloud.android.presentation.common.UIResult
import com.owncloud.android.providers.AccountProvider
import com.owncloud.android.providers.CoroutinesDispatcherProvider
import com.owncloud.android.usecases.files.RemoveLocalFilesForAccountUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ManageAccountsViewModel(
    private val accountProvider: AccountProvider,
    private val removeLocalFilesForAccountUseCase: RemoveLocalFilesForAccountUseCase,
    private val getCameraUploadsConfigurationUseCase: GetCameraUploadsConfigurationUseCase,
    private val coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
) : ViewModel() {

    private val _cleanAccountLocalStorageFlow = MutableStateFlow<Event<UIResult<Unit>>?>(null)
    val cleanAccountLocalStorageFlow: StateFlow<Event<UIResult<Unit>>?> = _cleanAccountLocalStorageFlow

    private var cameraUploadsConfiguration: CameraUploadsConfiguration? = null

    init {
        viewModelScope.launch(coroutinesDispatcherProvider.io) {
            cameraUploadsConfiguration = getCameraUploadsConfigurationUseCase(Unit).getDataOrNull()
        }
    }

    fun getLoggedAccounts(): Array<Account> {
        return accountProvider.getLoggedAccounts()
    }

    fun getCurrentAccount(): Account? {
        return accountProvider.getCurrentOwnCloudAccount()
    }

    fun cleanAccountLocalStorage(accountName: String) {
        runUseCaseWithResult(
            coroutineDispatcher = coroutinesDispatcherProvider.io,
            showLoading = true,
            flow = _cleanAccountLocalStorageFlow,
            useCase = removeLocalFilesForAccountUseCase,
            useCaseParams = RemoveLocalFilesForAccountUseCase.Params(accountName),
        )
    }

    fun hasCameraUploadsAttached(accountName: String): Boolean {
        return accountName == cameraUploadsConfiguration?.pictureUploadsConfiguration?.accountName ||
                accountName == cameraUploadsConfiguration?.videoUploadsConfiguration?.accountName
    }
}
