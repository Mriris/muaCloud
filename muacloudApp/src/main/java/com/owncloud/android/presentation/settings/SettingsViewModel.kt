

package com.owncloud.android.presentation.settings

import androidx.lifecycle.ViewModel
import com.owncloud.android.providers.AccountProvider

class SettingsViewModel(
    private val accountProvider: AccountProvider
) : ViewModel() {

    fun isThereAttachedAccount() = accountProvider.getCurrentOwnCloudAccount() != null
}
