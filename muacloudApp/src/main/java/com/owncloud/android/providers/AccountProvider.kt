
package com.owncloud.android.providers

import android.accounts.Account
import android.content.Context
import com.owncloud.android.presentation.authentication.AccountUtils

class AccountProvider(
    private val context: Context
) {
    fun getCurrentOwnCloudAccount(): Account? = AccountUtils.getCurrentOwnCloudAccount(context)
    fun getLoggedAccounts(): Array<Account> = AccountUtils.getAccounts(context)
}
