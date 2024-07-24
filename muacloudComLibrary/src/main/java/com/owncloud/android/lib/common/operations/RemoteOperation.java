/* ownCloud Android Library is available under MIT license
 *   Copyright (C) 2022 ownCloud GmbH.
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

package com.owncloud.android.lib.common.operations;

import android.accounts.Account;
import android.accounts.AccountsException;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.os.Handler;

import com.owncloud.android.lib.common.OwnCloudAccount;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.SingleSessionManager;
import com.owncloud.android.lib.common.accounts.AccountUtils;
import okhttp3.OkHttpClient;
import timber.log.Timber;

import java.io.IOException;

@SuppressWarnings("WeakerAccess")
public abstract class RemoteOperation<T> implements Runnable {


    public static final String OCS_API_HEADER = "OCS-APIREQUEST";

    public static final String OCS_API_HEADER_VALUE = "true";

    protected Account mAccount = null;


    protected Context mContext = null;


    private OwnCloudClient mClient = null;


    private OkHttpClient mHttpClient = null;


    private OnRemoteOperationListener mListener = null;


    private Handler mListenerHandler = null;


    public Thread execute(Account account, Context context,
                          OnRemoteOperationListener listener, Handler listenerHandler) {

        if (account == null) {
            throw new IllegalArgumentException("Trying to execute a remote operation with a NULL Account");
        }
        if (context == null) {
            throw new IllegalArgumentException("Trying to execute a remote operation with a NULL Context");
        }
        // mAccount and mContext in the runnerThread to create below
        mAccount = account;
        mContext = context.getApplicationContext();
        mClient = null;     // the client instance will be created from

        mListener = listener;

        mListenerHandler = listenerHandler;

        Thread runnerThread = new Thread(this);
        runnerThread.start();
        return runnerThread;
    }


    public Thread execute(OwnCloudClient client, OnRemoteOperationListener listener, Handler listenerHandler) {
        if (client == null) {
            throw new IllegalArgumentException("Trying to execute a remote operation with a NULL OwnCloudClient");
        }
        mClient = client;
        if (client.getAccount() != null) {
            mAccount = client.getAccount().getSavedAccount();
        }
        mContext = client.getContext();

        if (listener == null) {
            throw new IllegalArgumentException
                    ("Trying to execute a remote operation asynchronously without a listener to notify the result");
        }
        mListener = listener;

        if (listenerHandler != null) {
            mListenerHandler = listenerHandler;
        }

        Thread runnerThread = new Thread(this);
        runnerThread.start();
        return runnerThread;
    }

    private void grantOwnCloudClient() throws
            AccountUtils.AccountNotFoundException, OperationCanceledException, AuthenticatorException, IOException {
        if (mClient == null) {
            if (mAccount != null && mContext != null) {
                OwnCloudAccount ocAccount = new OwnCloudAccount(mAccount, mContext);
                mClient = SingleSessionManager.getDefaultSingleton().
                        getClientFor(ocAccount, mContext, SingleSessionManager.getConnectionValidator());
            } else {
                throw new IllegalStateException("Trying to run a remote operation " +
                        "asynchronously with no client and no chance to create one (no account)");
            }
        }
    }


    public final OwnCloudClient getClient() {
        return mClient;
    }


    protected abstract RemoteOperationResult<T> run(OwnCloudClient client);


    public RemoteOperationResult<T> execute(Account account, Context context) {
        if (account == null) {
            throw new IllegalArgumentException("Trying to execute a remote operation with a NULL Account");
        }
        if (context == null) {
            throw new IllegalArgumentException("Trying to execute a remote operation with a NULL Context");
        }
        mAccount = account;
        mContext = context.getApplicationContext();

        return runOperation();
    }


    public RemoteOperationResult<T> execute(OwnCloudClient client) {
        if (client == null) {
            throw new IllegalArgumentException("Trying to execute a remote operation with a NULL OwnCloudClient");
        }
        mClient = client;
        if (client.getAccount() != null) {
            mAccount = client.getAccount().getSavedAccount();
        }
        mContext = client.getContext();

        return runOperation();
    }


    public RemoteOperationResult<T> execute(OkHttpClient client, Context context) {
        if (client == null) {
            throw new IllegalArgumentException("Trying to execute a remote operation with a NULL OwnCloudClient");
        }
        mHttpClient = client;
        mContext = context;

        return runOperation();
    }


    private RemoteOperationResult<T> runOperation() {

        RemoteOperationResult<T> result;

        try {
            grantOwnCloudClient();
            result = run(mClient);

        } catch (AccountsException | IOException e) {
            Timber.e(e, "Error while trying to access to %s", mAccount.name);
            result = new RemoteOperationResult<>(e);
        }

        return result;
    }


    @Override
    public final void run() {

        final RemoteOperationResult resultToSend = runOperation();

        if (mListenerHandler != null && mListener != null) {
            mListenerHandler.post(() ->
                    mListener.onRemoteOperationFinish(RemoteOperation.this, resultToSend));
        } else if (mListener != null) {
            mListener.onRemoteOperationFinish(RemoteOperation.this, resultToSend);
        }
    }
}
