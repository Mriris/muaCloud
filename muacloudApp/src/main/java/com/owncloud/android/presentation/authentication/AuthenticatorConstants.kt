
@file:JvmName("AuthenticatorConstants")

package com.owncloud.android.presentation.authentication

import com.owncloud.android.MainApp.Companion.accountType
import com.owncloud.android.lib.common.accounts.AccountTypeUtils

const val EXTRA_ACTION = "ACTION"
const val EXTRA_ACCOUNT = "ACCOUNT"

const val ACTION_CREATE: Byte = 0
const val ACTION_UPDATE_TOKEN: Byte = 1 // requested by the user
const val ACTION_UPDATE_EXPIRED_TOKEN: Byte = 2 // detected by the app

const val KEY_AUTH_TOKEN_TYPE = "authTokenType"

val BASIC_TOKEN_TYPE: String = AccountTypeUtils.getAuthTokenTypePass(
    accountType
)

val OAUTH_TOKEN_TYPE: String = AccountTypeUtils.getAuthTokenTypeAccessToken(
    accountType
)

const val UNTRUSTED_CERT_DIALOG_TAG = "UNTRUSTED_CERT_DIALOG"
const val WAIT_DIALOG_TAG = "WAIT_DIALOG"
