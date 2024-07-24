

package com.owncloud.android.presentation.capabilities

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.owncloud.android.domain.capabilities.model.OCCapability
import com.owncloud.android.domain.capabilities.usecases.GetCapabilitiesAsLiveDataUseCase
import com.owncloud.android.domain.capabilities.usecases.RefreshCapabilitiesFromServerAsyncUseCase
import com.owncloud.android.domain.utils.Event
import com.owncloud.android.extensions.ViewModelExt.runUseCaseWithResultAndUseCachedData
import com.owncloud.android.presentation.common.UIResult
import com.owncloud.android.providers.CoroutinesDispatcherProvider


class CapabilityViewModel(
    private val accountName: String,
    getCapabilitiesAsLiveDataUseCase: GetCapabilitiesAsLiveDataUseCase,
    private val refreshCapabilitiesFromServerAsyncUseCase: RefreshCapabilitiesFromServerAsyncUseCase,
    private val coroutineDispatcherProvider: CoroutinesDispatcherProvider
) : ViewModel() {

    private val _capabilities = MediatorLiveData<Event<UIResult<OCCapability>>>()
    val capabilities: LiveData<Event<UIResult<OCCapability>>> = _capabilities

    private var capabilitiesLiveData: LiveData<OCCapability?> = getCapabilitiesAsLiveDataUseCase(
        GetCapabilitiesAsLiveDataUseCase.Params(
            accountName = accountName
        )
    )

    init {
        _capabilities.addSource(capabilitiesLiveData) { capabilities ->
            _capabilities.postValue(Event(UIResult.Success(capabilities)))
        }

        refreshCapabilitiesFromNetwork()
    }

    fun refreshCapabilitiesFromNetwork() = runUseCaseWithResultAndUseCachedData(
        coroutineDispatcher = coroutineDispatcherProvider.io,
        cachedData = capabilitiesLiveData.value,
        liveData = _capabilities,
        useCase = refreshCapabilitiesFromServerAsyncUseCase,
        useCaseParams = RefreshCapabilitiesFromServerAsyncUseCase.Params(
            accountName = accountName
        )
    )
}
