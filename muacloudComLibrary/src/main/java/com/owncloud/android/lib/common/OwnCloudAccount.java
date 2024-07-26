
package com.owncloud.android.lib.common;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.net.Uri;

import com.owncloud.android.lib.common.accounts.AccountUtils;
import com.owncloud.android.lib.common.accounts.AccountUtils.AccountNotFoundException;
import com.owncloud.android.lib.common.authentication.OwnCloudCredentials;
import com.owncloud.android.lib.common.authentication.OwnCloudCredentialsFactory;

import java.io.IOException;


public class OwnCloudAccount {

    private Uri mBaseUri;

    private OwnCloudCredentials mCredentials;

    private String mDisplayName;

    private String mSavedAccountName;

    private Account mSavedAccount;


    public OwnCloudAccount(Account savedAccount, Context context) throws AccountNotFoundException {
        if (savedAccount == null) {
            throw new IllegalArgumentException("Parameter 'savedAccount' cannot be null");
        }

        if (context == null) {
            throw new IllegalArgumentException("Parameter 'context' cannot be null");
        }

        mSavedAccount = savedAccount;
        mSavedAccountName = savedAccount.name;
        mCredentials = null;    // load of credentials is delayed

        AccountManager ama = AccountManager.get(context.getApplicationContext());
        String baseUrl = ama.getUserData(mSavedAccount, AccountUtils.Constants.KEY_OC_BASE_URL);
        if (baseUrl == null) {
            throw new AccountNotFoundException(mSavedAccount, "Account not found", null);
        }
        mBaseUri = Uri.parse(AccountUtils.getBaseUrlForAccount(context, mSavedAccount));
        mDisplayName = ama.getUserData(mSavedAccount, AccountUtils.Constants.KEY_DISPLAY_NAME);
    }


    public OwnCloudAccount(Uri baseUri, OwnCloudCredentials credentials) {
        if (baseUri == null) {
            throw new IllegalArgumentException("Parameter 'baseUri' cannot be null");
        }
        mSavedAccount = null;
        mSavedAccountName = null;
        mBaseUri = baseUri;
        mCredentials = credentials != null ?
                credentials : OwnCloudCredentialsFactory.getAnonymousCredentials();
        String username = mCredentials.getUsername();
        if (username != null) {
            mSavedAccountName = AccountUtils.buildAccountName(mBaseUri, username);
        }
    }


    public void loadCredentials(Context context) throws AuthenticatorException, IOException, OperationCanceledException {

        if (context == null) {
            throw new IllegalArgumentException("Parameter 'context' cannot be null");
        }

        if (mSavedAccount != null) {
            mCredentials = AccountUtils.getCredentialsForAccount(context, mSavedAccount);
        }
    }

    public Uri getBaseUri() {
        return mBaseUri;
    }

    public OwnCloudCredentials getCredentials() {
        return mCredentials;
    }

    public String getName() {
        return mSavedAccountName;
    }

    public Account getSavedAccount() {
        return mSavedAccount;
    }

    public String getDisplayName() {
        if (mDisplayName != null && mDisplayName.length() > 0) {
            return mDisplayName;
        } else if (mCredentials != null) {
            return mCredentials.getUsername();
        } else if (mSavedAccount != null) {
            return AccountUtils.getUsernameForAccount(mSavedAccount);
        } else {
            return null;
        }
    }
}