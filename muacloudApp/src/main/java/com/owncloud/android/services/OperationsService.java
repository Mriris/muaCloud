
package com.owncloud.android.services;

import android.accounts.Account;
import android.accounts.AccountsException;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Pair;

import com.owncloud.android.datamodel.FileDataStorageManager;
import com.owncloud.android.lib.common.OwnCloudAccount;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.SingleSessionManager;
import com.owncloud.android.lib.common.authentication.OwnCloudCredentials;
import com.owncloud.android.lib.common.operations.OnRemoteOperationListener;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.operations.CheckCurrentCredentialsOperation;
import com.owncloud.android.operations.common.SyncOperation;
import timber.log.Timber;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

public class OperationsService extends Service {

    public static final String EXTRA_ACCOUNT = "ACCOUNT";
    public static final String EXTRA_SERVER_URL = "SERVER_URL";
    public static final String EXTRA_FILE = "FILE";

    public static final String ACTION_CHECK_CURRENT_CREDENTIALS = "CHECK_CURRENT_CREDENTIALS";

    private final ConcurrentMap<Integer, Pair<RemoteOperation, RemoteOperationResult>>
            mUndispatchedFinishedOperations = new ConcurrentHashMap<>();

    private static class Target {
        public Uri mServerUrl;
        public Account mAccount;

        public Target(Account account, Uri serverUrl) {
            mAccount = account;
            mServerUrl = serverUrl;
        }
    }

    private ServiceHandler mOperationsHandler;
    private OperationsServiceBinder mOperationsBinder;


    @Override
    public void onCreate() {
        super.onCreate();
        Timber.d("Creating service");

        HandlerThread thread = new HandlerThread("Operations thread", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        mOperationsHandler = new ServiceHandler(thread.getLooper(), this);
        mOperationsBinder = new OperationsServiceBinder(mOperationsHandler);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timber.d("Starting command with id %s", startId);

        Message msg = mOperationsHandler.obtainMessage();
        msg.arg1 = startId;
        mOperationsHandler.sendMessage(msg);

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Timber.v("Destroying service");

        mUndispatchedFinishedOperations.clear();

        mOperationsBinder = null;

        mOperationsHandler.getLooper().quit();
        mOperationsHandler = null;

        super.onDestroy();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return mOperationsBinder;
    }


    @Override
    public boolean onUnbind(Intent intent) {
        mOperationsBinder.clearListeners();
        return false;   // not accepting rebinding (default behaviour)
    }


    public class OperationsServiceBinder extends Binder {


        private final ConcurrentMap<OnRemoteOperationListener, Handler> mBoundListeners = new ConcurrentHashMap<>();

        private final ServiceHandler mServiceHandler;

        OperationsServiceBinder(ServiceHandler serviceHandler) {
            mServiceHandler = serviceHandler;
        }

        void clearListeners() {
            mBoundListeners.clear();
        }


        public void addOperationListener(OnRemoteOperationListener listener, Handler callbackHandler) {
            synchronized (mBoundListeners) {
                mBoundListeners.put(listener, callbackHandler);
            }
        }


        public void removeOperationListener(OnRemoteOperationListener listener) {
            synchronized (mBoundListeners) {
                mBoundListeners.remove(listener);
            }
        }


        public long queueNewOperation(Intent operationIntent) {
            Pair<Target, RemoteOperation> itemToQueue = newOperation(operationIntent);
            if (itemToQueue != null) {
                mServiceHandler.mPendingOperations.add(itemToQueue);
                Intent executeOperation = new Intent(OperationsService.this, OperationsService.class);
                startService(executeOperation);
                return itemToQueue.second.hashCode();
            } else {
                return Long.MAX_VALUE;
            }
        }

        public boolean dispatchResultIfFinished(int operationId, OnRemoteOperationListener listener) {
            Pair<RemoteOperation, RemoteOperationResult> undispatched =
                    mUndispatchedFinishedOperations.remove(operationId);
            if (undispatched != null) {
                listener.onRemoteOperationFinish(undispatched.first, undispatched.second);
                return true;
            } else {
                return !mServiceHandler.mPendingOperations.isEmpty();
            }
        }
    }


    private static class ServiceHandler extends Handler {



        OperationsService mService;

        private final ConcurrentLinkedQueue<Pair<Target, RemoteOperation>> mPendingOperations =
                new ConcurrentLinkedQueue<>();
        private Target mLastTarget = null;
        private OwnCloudClient mOwnCloudClient = null;
        private FileDataStorageManager mStorageManager;

        ServiceHandler(Looper looper, OperationsService service) {
            super(looper);
            if (service == null) {
                throw new IllegalArgumentException("Received invalid NULL in parameter 'service'");
            }
            mService = service;
        }

        @Override
        public void handleMessage(Message msg) {
            nextOperation();
            Timber.d("Stopping after command with id %s", msg.arg1);
            mService.stopSelf(msg.arg1);
        }


        private void nextOperation() {
            Pair<Target, RemoteOperation> next;
            synchronized (mPendingOperations) {
                next = mPendingOperations.peek();
            }

            if (next != null) {

                RemoteOperation currentOperation = next.second;
                RemoteOperationResult result;
                try {

                    if (mLastTarget == null || !mLastTarget.equals(next.first)) {
                        mLastTarget = next.first;
                        OwnCloudAccount ocAccount;
                        if (mLastTarget.mAccount != null) {
                            ocAccount = new OwnCloudAccount(mLastTarget.mAccount, mService);
                            mOwnCloudClient = SingleSessionManager.getDefaultSingleton().
                                    getClientFor(ocAccount, mService);

                            mStorageManager = new FileDataStorageManager(mLastTarget.mAccount);
                        } else {
                            OwnCloudCredentials credentials = null;
                            ocAccount = new OwnCloudAccount(mLastTarget.mServerUrl, credentials);

                            mOwnCloudClient = SingleSessionManager.getDefaultSingleton().
                                    getClientFor(ocAccount, mService);

                            mStorageManager = null;
                        }
                    }

                    if (currentOperation instanceof SyncOperation) {
                        result = ((SyncOperation) currentOperation).execute(mOwnCloudClient, mStorageManager);
                    } else {
                        result = currentOperation.execute(mOwnCloudClient);
                    }

                } catch (AccountsException | IOException e) {
                    if (mLastTarget.mAccount == null) {
                        Timber.e(e, "Error while trying to get authorization for a NULL account");
                    } else {
                        Timber.e(e, "Error while trying to get authorization for %s", mLastTarget.mAccount.name);
                    }
                    result = new RemoteOperationResult(e);

                } catch (Exception e) {
                    if (mLastTarget.mAccount == null) {
                        Timber.e(e, "Unexpected error for a NULL account");
                    } else {
                        Timber.e(e, "Unexpected error for %s", mLastTarget.mAccount.name);
                    }
                    result = new RemoteOperationResult(e);

                } finally {
                    synchronized (mPendingOperations) {
                        mPendingOperations.poll();
                    }
                }

                mService.dispatchResultToOperationListeners(currentOperation, result);
            }
        }
    }

    
    private Pair<Target, RemoteOperation> newOperation(Intent operationIntent) {
        RemoteOperation operation = null;
        Target target = null;
        try {
            if (!operationIntent.hasExtra(EXTRA_ACCOUNT) &&
                    !operationIntent.hasExtra(EXTRA_SERVER_URL)) {
                Timber.e("Not enough information provided in intent");

            } else {
                Account account = operationIntent.getParcelableExtra(EXTRA_ACCOUNT);
                String serverUrl = operationIntent.getStringExtra(EXTRA_SERVER_URL);
                target = new Target(
                        account,
                        (serverUrl == null) ? null : Uri.parse(serverUrl)
                );

                String action = operationIntent.getAction();
                if (action != null) {
                    switch (action) {
                        case ACTION_CHECK_CURRENT_CREDENTIALS:

                            operation = new CheckCurrentCredentialsOperation(account);

                            break;
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            Timber.e(e, "Bad information provided in intent: %s", e.getMessage());
            operation = null;
        }

        if (operation != null) {
            return new Pair<>(target, operation);
        } else {
            return null;
        }
    }

    
    protected void dispatchResultToOperationListeners(
            final RemoteOperation operation, final RemoteOperationResult result
    ) {
        int count = 0;
        for (OnRemoteOperationListener listener : mOperationsBinder.mBoundListeners.keySet()) {
            final Handler handler = mOperationsBinder.mBoundListeners.get(listener);
            if (handler != null) {
                handler.post(() -> listener.onRemoteOperationFinish(operation, result));
                count += 1;
            }
        }
        if (count == 0) {
            Pair<RemoteOperation, RemoteOperationResult> undispatched = new Pair<>(operation, result);
            mUndispatchedFinishedOperations.put(operation.hashCode(), undispatched);
        }
        Timber.d("Called " + count + " listeners");
    }
}
