

package com.owncloud.android.extensions

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.owncloud.android.domain.BaseUseCaseWithResult
import com.owncloud.android.domain.exceptions.NoNetworkConnectionException
import com.owncloud.android.domain.utils.Event
import com.owncloud.android.presentation.common.UIResult
import com.owncloud.android.providers.ContextProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

object ViewModelExt : KoinComponent {

    private val contextProvider: ContextProvider by inject()

    fun <T, Params> ViewModel.runUseCaseWithResult(
        coroutineDispatcher: CoroutineDispatcher,
        requiresConnection: Boolean = true,
        showLoading: Boolean = false,
        liveData: MediatorLiveData<Event<UIResult<T>>>,
        useCase: BaseUseCaseWithResult<T, Params>,
        useCaseParams: Params,
        postSuccess: Boolean = true,
        postSuccessWithData: Boolean = true
    ) {
        viewModelScope.launch(coroutineDispatcher) {
            if (showLoading) {
                liveData.postValue(Event(UIResult.Loading()))
            }

            if (requiresConnection and !contextProvider.isConnected()) {
                liveData.postValue(Event(UIResult.Error(error = NoNetworkConnectionException())))
                Timber.w("${useCase.javaClass.simpleName} will not be executed due to lack of network connection")
                return@launch
            }

            val useCaseResult = useCase(useCaseParams)

            Timber.d("Use case executed: ${useCase.javaClass.simpleName} with result: $useCaseResult")

            if (useCaseResult.isSuccess && postSuccess) {
                if (postSuccessWithData) {
                    liveData.postValue(Event(UIResult.Success(useCaseResult.getDataOrNull())))
                } else {
                    liveData.postValue(Event(UIResult.Success()))
                }
            } else if (useCaseResult.isError) {
                liveData.postValue(Event(UIResult.Error(error = useCaseResult.getThrowableOrNull())))
            }
        }
    }

    fun <T, Params> ViewModel.runUseCaseWithResult(
        coroutineDispatcher: CoroutineDispatcher,
        requiresConnection: Boolean = true,
        showLoading: Boolean = false,
        flow: MutableStateFlow<Event<UIResult<T>>?>,
        useCase: BaseUseCaseWithResult<T, Params>,
        useCaseParams: Params,
        postSuccess: Boolean = true,
        postSuccessWithData: Boolean = true
    ) {
        viewModelScope.launch(coroutineDispatcher) {
            if (showLoading) {
                flow.update { Event(UIResult.Loading()) }
            }

            if (requiresConnection and !contextProvider.isConnected()) {
                flow.update { Event(UIResult.Error(error = NoNetworkConnectionException())) }
                Timber.w("${useCase.javaClass.simpleName} will not be executed due to lack of network connection")
                return@launch
            }

            val useCaseResult = useCase(useCaseParams)

            Timber.d("Use case executed: ${useCase.javaClass.simpleName} with result: $useCaseResult")

            if (useCaseResult.isSuccess && postSuccess) {
                if (postSuccessWithData) {
                    flow.update { Event(UIResult.Success(useCaseResult.getDataOrNull())) }
                } else {
                    flow.update { Event(UIResult.Success()) }
                }
            } else if (useCaseResult.isError) {
                flow.update { Event(UIResult.Error(error = useCaseResult.getThrowableOrNull())) }
            }
        }
    }

    fun <T, Params> ViewModel.runUseCaseWithResult(
        coroutineDispatcher: CoroutineDispatcher,
        requiresConnection: Boolean = true,
        showLoading: Boolean = false,
        sharedFlow: MutableSharedFlow<UIResult<T>>,
        useCase: BaseUseCaseWithResult<T, Params>,
        useCaseParams: Params,
        postSuccess: Boolean = true,
        postSuccessWithData: Boolean = true
    ) {
        viewModelScope.launch(coroutineDispatcher) {
            if (showLoading) {
                sharedFlow.emit(UIResult.Loading())
            }

            if (requiresConnection and !contextProvider.isConnected()) {
                sharedFlow.emit(UIResult.Error(error = NoNetworkConnectionException()))
                Timber.w("${useCase.javaClass.simpleName} will not be executed due to lack of network connection")
                return@launch
            }

            val useCaseResult = useCase(useCaseParams)
            Timber.d("Use case executed: ${useCase.javaClass.simpleName} with result: $useCaseResult")

            if (useCaseResult.isSuccess && postSuccess) {
                if (postSuccessWithData) {
                    sharedFlow.emit(UIResult.Success(useCaseResult.getDataOrNull()))
                } else {
                    sharedFlow.emit(UIResult.Success())
                }
            } else if (useCaseResult.isError) {
                sharedFlow.emit(UIResult.Error(error = useCaseResult.getThrowableOrNull()))
            }
        }
    }

    fun <T, U, Params> ViewModel.runUseCaseWithResultAndUseCachedData(
        coroutineDispatcher: CoroutineDispatcher,
        requiresConnection: Boolean = true,
        cachedData: T?,
        liveData: MediatorLiveData<Event<UIResult<T>>>,
        useCase: BaseUseCaseWithResult<U, Params>,
        useCaseParams: Params
    ) {
        viewModelScope.launch(coroutineDispatcher) {
            liveData.postValue(Event(UIResult.Loading(cachedData)))

            if (requiresConnection && !contextProvider.isConnected()) {
                liveData.postValue(Event(UIResult.Error(error = NoNetworkConnectionException(), data = cachedData)))
                Timber.w("${useCase.javaClass.simpleName} will not be executed due to lack of network connection")
                return@launch
            }

            val useCaseResult = useCase(useCaseParams)

            Timber.d("Use case executed: ${useCase.javaClass.simpleName} with result: $useCaseResult")

            if (useCaseResult.isError) {
                liveData.postValue(Event(UIResult.Error(error = useCaseResult.getThrowableOrNull(), data = cachedData)))
            }
        }
    }
}
