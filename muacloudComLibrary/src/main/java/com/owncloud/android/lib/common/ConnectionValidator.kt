/* ownCloud Android Library is available under MIT license
 *   Copyright (C) 2016 ownCloud GmbH.
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *   EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 *   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 *   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *   THE SOFTWARE.
 *
 */

package com.owncloud.android.lib.common

import android.accounts.AccountManager
import android.accounts.AccountsException
import android.content.Context
import com.owncloud.android.lib.common.authentication.OwnCloudCredentials
import com.owncloud.android.lib.common.authentication.OwnCloudCredentialsFactory.OwnCloudAnonymousCredentials
import com.owncloud.android.lib.common.http.HttpConstants
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.resources.files.CheckPathExistenceRemoteOperation
import com.owncloud.android.lib.resources.status.GetRemoteStatusOperation
import com.owncloud.android.lib.resources.status.RemoteServerInfo
import org.apache.commons.lang3.exception.ExceptionUtils
import timber.log.Timber
import java.io.IOException


class ConnectionValidator(
    val context: Context,
    private val clearCookiesOnValidation: Boolean
) {
    fun validate(baseClient: OwnCloudClient, singleSessionManager: SingleSessionManager, context: Context): Boolean {
        try {
            var validationRetryCount = 0
            val client = OwnCloudClient(baseClient.baseUri, null, false, singleSessionManager, context)
            if (clearCookiesOnValidation) {
                client.clearCookies()
            } else {
                client.cookiesForBaseUri = baseClient.cookiesForBaseUri
            }

            client.account = baseClient.account
            client.credentials = baseClient.credentials
            while (validationRetryCount < VALIDATION_RETRY_COUNT) {
                Timber.d("validationRetryCount %d", validationRetryCount)
                var successCounter = 0
                var failCounter = 0

                client.setFollowRedirects(true)
                if (isOwnCloudStatusOk(client)) {
                    successCounter++
                } else {
                    failCounter++
                }

                // Skip the part where we try to check if we can access the parts where we have to be logged in... if we are not logged in
                if (baseClient.credentials !is OwnCloudAnonymousCredentials) {
                    client.setFollowRedirects(false)
                    val contentReply = canAccessRootFolder(client)
                    if (contentReply.httpCode == HttpConstants.HTTP_OK) {
                        if (contentReply.data == true) { //if data is true it means that the content reply was ok
                            successCounter++
                        } else {
                            failCounter++
                        }
                    } else {
                        failCounter++
                        if (contentReply.httpCode == HttpConstants.HTTP_UNAUTHORIZED) {
                            checkUnauthorizedAccess(client, singleSessionManager, contentReply.httpCode)
                        }
                    }
                }
                if (successCounter >= failCounter) {
                    baseClient.credentials = client.credentials
                    baseClient.cookiesForBaseUri = client.cookiesForBaseUri
                    return true
                }
                validationRetryCount++
            }
            Timber.d("Could not authenticate or get valid data from owncloud")
        } catch (e: Exception) {
            Timber.d(ExceptionUtils.getStackTrace(e))
        }
        return false
    }

    private fun isOwnCloudStatusOk(client: OwnCloudClient): Boolean {
        val reply = getOwnCloudStatus(client)
        // dont check status code. It currently relais on the broken redirect code of the owncloud client
        // TODO: Use okhttp redirect and add this check again
        // return reply.httpCode == HttpConstants.HTTP_OK &&
        return !reply.isException &&
                reply.data != null
    }

    private fun getOwnCloudStatus(client: OwnCloudClient): RemoteOperationResult<RemoteServerInfo> {
        val remoteStatusOperation = GetRemoteStatusOperation()
        return remoteStatusOperation.execute(client)
    }

    private fun canAccessRootFolder(client: OwnCloudClient): RemoteOperationResult<Boolean> {
        val checkPathExistenceRemoteOperation = CheckPathExistenceRemoteOperation("/", true)
        return checkPathExistenceRemoteOperation.execute(client)
    }

    
    private fun shouldInvalidateAccountCredentials(credentials: OwnCloudCredentials, account: OwnCloudAccount, httpStatusCode: Int): Boolean {
        var shouldInvalidateAccountCredentials = httpStatusCode == HttpConstants.HTTP_UNAUTHORIZED
        shouldInvalidateAccountCredentials = shouldInvalidateAccountCredentials and  // real credentials
                (credentials !is OwnCloudAnonymousCredentials)

        // test if have all the needed to effectively invalidate ...
        shouldInvalidateAccountCredentials =
            shouldInvalidateAccountCredentials and (account.savedAccount != null)
        Timber.d(
            """Received error: $httpStatusCode,
            account: ${account.name}
            credentials are real: ${credentials !is OwnCloudAnonymousCredentials},
            so we need to invalidate credentials for account ${account.name} : $shouldInvalidateAccountCredentials"""
        )
        return shouldInvalidateAccountCredentials
    }

    
    private fun invalidateAccountCredentials(account: OwnCloudAccount, credentials: OwnCloudCredentials) {
        Timber.i("Invalidating account credentials for account $account")
        val am = AccountManager.get(context)
        am.invalidateAuthToken(
            account.savedAccount.type,
            credentials.authToken
        )
        am.clearPassword(account.savedAccount) // being strict, only needed for Basic Auth credentials
    }

    
    private fun checkUnauthorizedAccess(client: OwnCloudClient, singleSessionManager: SingleSessionManager, status: Int): Boolean {
        var credentialsWereRefreshed = false
        val account = client.account
        val credentials = account.credentials
        if (shouldInvalidateAccountCredentials(credentials, account, status)) {
            invalidateAccountCredentials(account, credentials)

            if (credentials.authTokenCanBeRefreshed()) {
                try {
                    // This command does the actual refresh
                    Timber.i("Trying to refresh auth token for account $account")
                    account.loadCredentials(context)
                    // if mAccount.getCredentials().length() == 0 --> refresh failed
                    client.credentials = account.credentials
                    credentialsWereRefreshed = true
                } catch (e: AccountsException) {
                    Timber.e(
                        e, "Error while trying to refresh auth token for %s\ntrace: %s",
                        account.savedAccount.name,
                        ExceptionUtils.getStackTrace(e)
                    )
                } catch (e: IOException) {
                    Timber.e(
                        e, "Error while trying to refresh auth token for %s\ntrace: %s",
                        account.savedAccount.name,
                        ExceptionUtils.getStackTrace(e)
                    )
                }
                if (!credentialsWereRefreshed) {
                    // if credentials are not refreshed, client must be removed
                    // from the OwnCloudClientManager to prevent it is reused once and again
                    Timber.w("Credentials were not refreshed, client will be removed from the Session Manager to prevent using it over and over")
                    singleSessionManager.removeClientFor(account)
                }
            }
            // else: onExecute will finish with status 401
        }
        return credentialsWereRefreshed
    }

    companion object {
        private const val VALIDATION_RETRY_COUNT = 3
    }
}
