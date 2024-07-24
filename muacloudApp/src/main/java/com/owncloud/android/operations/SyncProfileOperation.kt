
package com.owncloud.android.operations

import android.accounts.Account
import android.accounts.AccountManager
import com.owncloud.android.MainApp.Companion.appContext
import com.owncloud.android.domain.capabilities.usecases.GetStoredCapabilitiesUseCase
import com.owncloud.android.domain.user.usecases.GetUserAvatarAsyncUseCase
import com.owncloud.android.domain.user.usecases.GetUserInfoAsyncUseCase
import com.owncloud.android.domain.user.usecases.RefreshUserQuotaFromServerAsyncUseCase
import com.owncloud.android.lib.common.accounts.AccountUtils
import com.owncloud.android.presentation.avatar.AvatarManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber


class SyncProfileOperation(
    private val account: Account
) : KoinComponent {
    fun syncUserProfile() {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                val getUserInfoAsyncUseCase: GetUserInfoAsyncUseCase by inject()
                val userInfoResult = getUserInfoAsyncUseCase(GetUserInfoAsyncUseCase.Params(account.name))
                userInfoResult.getDataOrNull()?.let { userInfo ->
                    Timber.d("User info synchronized for account ${account.name}")

                    AccountManager.get(appContext).run {
                        setUserData(account, AccountUtils.Constants.KEY_DISPLAY_NAME, userInfo.displayName)
                        setUserData(account, AccountUtils.Constants.KEY_ID, userInfo.id)
                    }

                    val getStoredCapabilitiesUseCase: GetStoredCapabilitiesUseCase by inject()
                    val storedCapabilities = getStoredCapabilitiesUseCase(GetStoredCapabilitiesUseCase.Params(account.name))

                    storedCapabilities?.let {
                        if (!it.isSpacesAllowed()) {
                            val refreshUserQuotaFromServerAsyncUseCase: RefreshUserQuotaFromServerAsyncUseCase by inject()
                            val userQuotaResult =
                                refreshUserQuotaFromServerAsyncUseCase(
                                    RefreshUserQuotaFromServerAsyncUseCase.Params(
                                        account.name
                                    )
                                )
                            userQuotaResult.getDataOrNull()?.let {
                                Timber.d("User quota synchronized for oC10 account ${account.name}")
                            }
                        }
                    }
                    val shouldFetchAvatar = storedCapabilities?.isFetchingAvatarAllowed() ?: true
                    if (shouldFetchAvatar) {
                        val getUserAvatarAsyncUseCase: GetUserAvatarAsyncUseCase by inject()
                        val userAvatarResult = getUserAvatarAsyncUseCase(GetUserAvatarAsyncUseCase.Params(account.name))
                        AvatarManager().handleAvatarUseCaseResult(account, userAvatarResult)
                        if (userAvatarResult.isSuccess) {
                            Timber.d("Avatar synchronized for account ${account.name}")
                        }
                    } else {
                        Timber.d("Avatar for this account: ${account.name} won't be synced due to capabilities ")
                    }
                } ?: Timber.d("User profile was not synchronized")
            }
        } catch (e: Exception) {
            Timber.e(e, "Exception while getting user profile")
        }
    }
}
