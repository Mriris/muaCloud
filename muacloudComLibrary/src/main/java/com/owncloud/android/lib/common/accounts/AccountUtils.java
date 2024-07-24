/* ownCloud Android Library is available under MIT license
 *   Copyright (C) 2020 ownCloud GmbH.
 *   Copyright (C) 2012  Bartek Przybylski
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

package com.owncloud.android.lib.common.accounts;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountsException;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.net.Uri;

import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.authentication.OwnCloudCredentials;
import com.owncloud.android.lib.common.authentication.OwnCloudCredentialsFactory;
import com.owncloud.android.lib.resources.status.OwnCloudVersion;
import timber.log.Timber;

import java.io.IOException;

public class AccountUtils {
    
    public static String getWebDavUrlForAccount(Context context, Account account)
            throws AccountNotFoundException {

        return getBaseUrlForAccount(context, account) + OwnCloudClient.WEBDAV_FILES_PATH_4_0
                + AccountUtils.getUserId(account, context);
    }

    
    public static String getBaseUrlForAccount(Context context, Account account)
            throws AccountNotFoundException {
        AccountManager ama = AccountManager.get(context.getApplicationContext());
        String baseurl = ama.getUserData(account, Constants.KEY_OC_BASE_URL);

        if (baseurl == null) {
            throw new AccountNotFoundException(account, "Account not found", null);
        }

        return baseurl;
    }

    
    public static String getUsernameForAccount(Account account) {
        String username = null;
        try {
            username = account.name.substring(0, account.name.lastIndexOf('@'));
        } catch (Exception e) {
            Timber.e(e, "Couldn't get a username for the given account");
        }
        return username;
    }

    
    public static OwnCloudCredentials getCredentialsForAccount(Context context, Account account)
            throws OperationCanceledException, AuthenticatorException, IOException {

        OwnCloudCredentials credentials;
        AccountManager am = AccountManager.get(context);

        String supportsOAuth2 = am.getUserData(account, AccountUtils.Constants.KEY_SUPPORTS_OAUTH2);
        boolean isOauth2 = supportsOAuth2 != null && supportsOAuth2.equals(Constants.OAUTH_SUPPORTED_TRUE);

        String username = AccountUtils.getUsernameForAccount(account);

        if (isOauth2) {
            Timber.i("Trying to retrieve credentials for oAuth account" + account.name);
            String accessToken = am.blockingGetAuthToken(
                    account,
                    AccountTypeUtils.getAuthTokenTypeAccessToken(account.type),
                    false);

            credentials = OwnCloudCredentialsFactory.newBearerCredentials(username, accessToken);
        } else {
            String password = am.blockingGetAuthToken(
                    account,
                    AccountTypeUtils.getAuthTokenTypePass(account.type),
                    false);

            credentials = OwnCloudCredentialsFactory.newBasicCredentials(
                    username,
                    password
            );
        }

        return credentials;
    }

    
    public static String getUserId(Account account, Context context) {
        AccountManager accountMgr = AccountManager.get(context);
        return accountMgr.getUserData(account, Constants.KEY_ID);
    }

    public static String buildAccountNameOld(Uri serverBaseUrl, String username) {
        if (serverBaseUrl.getScheme() == null) {
            serverBaseUrl = Uri.parse("https://" + serverBaseUrl.toString());
        }
        String accountName = username + "@" + serverBaseUrl.getHost();
        if (serverBaseUrl.getPort() >= 0) {
            accountName += ":" + serverBaseUrl.getPort();
        }
        return accountName;
    }

    public static String buildAccountName(Uri serverBaseUrl, String username) {
        if (serverBaseUrl.getScheme() == null) {
            serverBaseUrl = Uri.parse("https://" + serverBaseUrl.toString());
        }

        // Remove http:// or https://
        String url = serverBaseUrl.toString();
        if (url.contains("://")) {
            url = url.substring(serverBaseUrl.toString().indexOf("://") + 3);
        }

        return username + "@" + url;
    }

    public static class AccountNotFoundException extends AccountsException {

        
        private static final long serialVersionUID = -1684392454798508693L;

        private Account mFailedAccount;

        public AccountNotFoundException() {
            super();
        }

        public AccountNotFoundException(Account failedAccount, String message, Throwable cause) {
            super(message, cause);
            mFailedAccount = failedAccount;
        }

        public Account getFailedAccount() {
            return mFailedAccount;
        }
    }

    public static class Constants {
        
        public static final String KEY_OC_BASE_URL = "oc_base_url";
        

        // TODO Please review this constants, move them out of the library, the rest of OAuth variables are in data layer
        public static final String KEY_SUPPORTS_OAUTH2 = "oc_supports_oauth2";

        public static final String OAUTH_SUPPORTED_TRUE = "TRUE";

        
        public static final String KEY_OC_ACCOUNT_VERSION = "oc_account_version";

        
        public static final String KEY_ID = "oc_id";

        
        public static final String KEY_DISPLAY_NAME = "oc_display_name";

        public static final int ACCOUNT_VERSION = 1;
    }
}
