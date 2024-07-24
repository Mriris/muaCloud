

package com.owncloud.android.syncadapter;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;

import com.owncloud.android.datamodel.FileDataStorageManager;
import com.owncloud.android.lib.common.OwnCloudAccount;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.SingleSessionManager;
import com.owncloud.android.lib.common.accounts.AccountUtils.AccountNotFoundException;

import java.io.IOException;


public abstract class AbstractOwnCloudSyncAdapter extends
        AbstractThreadedSyncAdapter {

    private AccountManager accountManager;
    private Account account;
    private ContentProviderClient mContentProviderClient;
    private FileDataStorageManager mStoreManager;

    private OwnCloudClient mClient = null;

    public AbstractOwnCloudSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        this.setAccountManager(AccountManager.get(context));
    }

    public AccountManager getAccountManager() {
        return accountManager;
    }

    public void setAccountManager(AccountManager accountManager) {
        this.accountManager = accountManager;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public ContentProviderClient getContentProviderClient() {
        return mContentProviderClient;
    }

    public void setContentProviderClient(ContentProviderClient contentProvider) {
        this.mContentProviderClient = contentProvider;
    }

    public void setStorageManager(FileDataStorageManager storage_manager) {
        mStoreManager = storage_manager;
    }

    public FileDataStorageManager getStorageManager() {
        return mStoreManager;
    }

    protected void initClientForCurrentAccount() throws OperationCanceledException,
            AuthenticatorException, IOException, AccountNotFoundException {
        OwnCloudAccount ocAccount = new OwnCloudAccount(account, getContext());
        mClient = SingleSessionManager.getDefaultSingleton().
                getClientFor(ocAccount, getContext());
    }

    protected OwnCloudClient getClient() {
        return mClient;
    }

}
