
package com.owncloud.android.testutil

import android.accounts.Account

const val OC_ACCOUNT_ID = "username"
const val OC_ACCOUNT_NAME = "$OC_ACCOUNT_ID@demo.owncloud.com"


val OC_ACCOUNT = getAccount()

fun getAccount(name: String = OC_ACCOUNT_NAME, type: String = "owncloud"): Account {
    val account = Account(name, type)
    // We need reflection or account will be Account(null, null) because name and type are final
    with(account.javaClass.getDeclaredField("name")) {
        isAccessible = true
        set(account, name)
    }
    with(account.javaClass.getDeclaredField("type")) {
        isAccessible = true
        set(account, type)
    }
    return account
}


const val OC_BASIC_USERNAME = "user"
const val OC_BASIC_PASSWORD = "password"


const val OC_OAUTH_SUPPORTED_TRUE = "TRUE"
const val OC_AUTH_TOKEN_TYPE = "owncloud.oauth2.access_token"
const val OC_ACCESS_TOKEN = "Asqweh12p93yehd10eu"
const val OC_REFRESH_TOKEN = "P3sd19DSsjdp1jwdd1"
const val OC_SCOPE = "email"
const val OC_REDIRECT_URI = "oc:android.owncloud.com"

const val OC_TOKEN_ENDPOINT = "https://owncloud.server/token"
const val OC_CLIENT_AUTH = "cl13nt4uth"

const val OC_CLIENT_SECRET = "cl13nts3cr3t"
const val OC_CLIENT_ID = "cl13nt1d"
const val OC_CLIENT_SECRET_EXPIRATION = 1611251163
