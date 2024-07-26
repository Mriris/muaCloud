

package com.owncloud.android.ui.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.OperationCanceledException;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.snackbar.Snackbar;
import com.owncloud.android.MainApp;
import com.owncloud.android.presentation.authentication.AccountUtils;
import com.owncloud.android.datamodel.FileDataStorageManager;
import com.owncloud.android.domain.files.model.OCFile;
import com.owncloud.android.ui.dialog.LoadingDialog;
import timber.log.Timber;


public abstract class BaseActivity extends AppCompatActivity {


    private Account mCurrentAccount;


    private boolean mRedirectingToSetupAccount = false;


    protected boolean mAccountWasSet;


    protected boolean mAccountWasRestored;


    private FileDataStorageManager mStorageManager = null;

    private static final String DIALOG_WAIT_TAG = "DIALOG_WAIT";

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Timber.v("onNewIntent() start");
        Account current = AccountUtils.getCurrentOwnCloudAccount(this);
        if (current != null && mCurrentAccount != null && !mCurrentAccount.name.equals(current.name)) {
            mCurrentAccount = current;
        }
        Timber.v("onNewIntent() stop");
    }


    @Override
    protected void onRestart() {
        Timber.v("onRestart() start");
        super.onRestart();
        boolean validAccount = (mCurrentAccount != null && AccountUtils.exists(mCurrentAccount.name, this));
        if (!validAccount) {
            swapToDefaultAccount();
        }
        Timber.v("onRestart() end");
    }


    protected void setAccount(Account account, boolean savedAccount) {
        Account oldAccount = mCurrentAccount;
        boolean validAccount =
                (account != null && AccountUtils.setCurrentOwnCloudAccount(getApplicationContext(),
                        account.name));
        if (validAccount) {
            mCurrentAccount = account;
            mAccountWasSet = true;
            mAccountWasRestored = (savedAccount || mCurrentAccount.equals(oldAccount));

        } else {
            swapToDefaultAccount();
        }
    }


    protected void swapToDefaultAccount() {

        Account newAccount = AccountUtils.getCurrentOwnCloudAccount(getApplicationContext());
        if (newAccount == null) {

            createAccount(true);
            mRedirectingToSetupAccount = true;
            mAccountWasSet = false;
            mAccountWasRestored = false;

        } else {
            mAccountWasSet = true;
            mAccountWasRestored = (newAccount.equals(mCurrentAccount));
            mCurrentAccount = newAccount;
        }
    }


    private void createAccount(boolean mandatoryCreation) {
        AccountManager am = AccountManager.get(getApplicationContext());
        am.addAccount(MainApp.Companion.getAccountType(),
                null,
                null,
                null,
                this,
                new AccountCreationCallback(mandatoryCreation),
                new Handler());
    }


    protected void onAccountSet(boolean stateWasRecovered) {
        if (getAccount() != null) {
            mStorageManager = new FileDataStorageManager(getAccount());
            Timber.d("Account set: %s", getAccount().name);
        } else {
            Timber.e("onAccountChanged was called with NULL account associated!");
        }
    }

    public void setAccount(Account account) {
        mCurrentAccount = account;
    }


    public Account getAccount() {
        return mCurrentAccount;
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mAccountWasSet) {
            onAccountSet(mAccountWasRestored);
        }
    }


    protected boolean isRedirectingToSetupAccount() {
        return mRedirectingToSetupAccount;
    }

    public FileDataStorageManager getStorageManager() {
        if (mStorageManager == null) {
            if (getAccount() == null) {
                swapToDefaultAccount();
            }
            return mStorageManager = new FileDataStorageManager(getAccount());
        } else {
            return mStorageManager;
        }
    }


    protected void onAccountCreationSuccessful(AccountManagerFuture<Bundle> future) {

    }


    public class AccountCreationCallback implements AccountManagerCallback<Bundle> {

        boolean mMandatoryCreation;


        public AccountCreationCallback(boolean mandatoryCreation) {
            mMandatoryCreation = mandatoryCreation;
        }

        @Override
        public void run(AccountManagerFuture<Bundle> future) {
            BaseActivity.this.mRedirectingToSetupAccount = false;
            boolean accountWasSet = false;
            if (future != null) {
                try {
                    Bundle result;
                    result = future.getResult();
                    String name = result.getString(AccountManager.KEY_ACCOUNT_NAME);
                    String type = result.getString(AccountManager.KEY_ACCOUNT_TYPE);
                    if (AccountUtils.setCurrentOwnCloudAccount(getApplicationContext(), name)) {
                        setAccount(new Account(name, type), false);
                        accountWasSet = true;
                    }

                    onAccountCreationSuccessful(future);
                } catch (OperationCanceledException e) {
                    Timber.d("Account creation canceled");

                } catch (Exception e) {
                    Timber.e(e, "Account creation finished in exception");
                }

            } else {
                Timber.e("Account creation callback with null bundle");
            }
            if (mMandatoryCreation && !accountWasSet) {
                finish();
            }
        }
    }


    public void showLoadingDialog(int messageId) {

        dismissLoadingDialog();

        Fragment frag = getSupportFragmentManager().findFragmentByTag(DIALOG_WAIT_TAG);
        if (frag == null) {
            Timber.d("show loading dialog");
            LoadingDialog loading = LoadingDialog.newInstance(messageId, false);
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            loading.show(ft, DIALOG_WAIT_TAG);
            fm.executePendingTransactions();
        }
    }


    public void dismissLoadingDialog() {
        Fragment frag = getSupportFragmentManager().findFragmentByTag(DIALOG_WAIT_TAG);
        if (frag == null) {
            return;
        }

        Timber.d("dismiss loading dialog");
        LoadingDialog loading = (LoadingDialog) frag;
        loading.dismiss();
    }


    public void showSnackMessage(String message) {
        final View rootView = findViewById(android.R.id.content);

        if (rootView == null) {

            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            return;
        }
        Snackbar.make(rootView, message, Snackbar.LENGTH_LONG).show();
    }
}
