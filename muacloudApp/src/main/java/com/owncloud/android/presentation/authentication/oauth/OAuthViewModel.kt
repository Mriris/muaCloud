

package com.owncloud.android.presentation.authentication.oauth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.owncloud.android.MainApp
import com.owncloud.android.domain.authentication.oauth.OIDCDiscoveryUseCase
import com.owncloud.android.domain.authentication.oauth.RegisterClientUseCase
import com.owncloud.android.domain.authentication.oauth.RequestTokenUseCase
import com.owncloud.android.domain.authentication.oauth.model.ClientRegistrationInfo
import com.owncloud.android.domain.authentication.oauth.model.OIDCServerConfiguration
import com.owncloud.android.domain.authentication.oauth.model.TokenRequest
import com.owncloud.android.domain.authentication.oauth.model.TokenResponse
import com.owncloud.android.domain.utils.Event
import com.owncloud.android.extensions.ViewModelExt.runUseCaseWithResult
import com.owncloud.android.presentation.common.UIResult
import com.owncloud.android.providers.CoroutinesDispatcherProvider

class OAuthViewModel(
    private val getOIDCDiscoveryUseCase: OIDCDiscoveryUseCase,
    private val requestTokenUseCase: RequestTokenUseCase,
    private val registerClientUseCase: RegisterClientUseCase,
    private val coroutinesDispatcherProvider: CoroutinesDispatcherProvider
) : ViewModel() {

    val codeVerifier: String = OAuthUtils().generateRandomCodeVerifier()
    val codeChallenge: String = OAuthUtils().generateCodeChallenge(codeVerifier)
    val oidcState: String = OAuthUtils().generateRandomState()

    private val _oidcDiscovery = MediatorLiveData<Event<UIResult<OIDCServerConfiguration>>>()
    val oidcDiscovery: LiveData<Event<UIResult<OIDCServerConfiguration>>> = _oidcDiscovery

    fun getOIDCServerConfiguration(
        serverUrl: String
    ) = runUseCaseWithResult(
        coroutineDispatcher = coroutinesDispatcherProvider.io,
        showLoading = false,
        liveData = _oidcDiscovery,
        useCase = getOIDCDiscoveryUseCase,
        useCaseParams = OIDCDiscoveryUseCase.Params(baseUrl = serverUrl)
    )

    private val _registerClient = MediatorLiveData<Event<UIResult<ClientRegistrationInfo>>>()
    val registerClient: LiveData<Event<UIResult<ClientRegistrationInfo>>> = _registerClient

    fun registerClient(
        registrationEndpoint: String
    ) {
        val registrationRequest = OAuthUtils.buildClientRegistrationRequest(
            registrationEndpoint = registrationEndpoint,
            MainApp.appContext
        )

        runUseCaseWithResult(
            coroutineDispatcher = coroutinesDispatcherProvider.io,
            showLoading = false,
            liveData = _registerClient,
            useCase = registerClientUseCase,
            useCaseParams = RegisterClientUseCase.Params(registrationRequest)
        )
    }

    private val _requestToken = MediatorLiveData<Event<UIResult<TokenResponse>>>()
    val requestToken: LiveData<Event<UIResult<TokenResponse>>> = _requestToken

    fun requestToken(
        tokenRequest: TokenRequest
    ) = runUseCaseWithResult(
        coroutineDispatcher = coroutinesDispatcherProvider.io,
        showLoading = false,
        liveData = _requestToken,
        useCase = requestTokenUseCase,
        useCaseParams = RequestTokenUseCase.Params(tokenRequest = tokenRequest)
    )
}
