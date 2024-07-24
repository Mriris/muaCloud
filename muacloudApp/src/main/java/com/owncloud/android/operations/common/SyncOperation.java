

package com.owncloud.android.operations.common;

import android.content.Context;
import android.os.Handler;

import com.owncloud.android.datamodel.FileDataStorageManager;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.operations.OnRemoteOperationListener;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;


public abstract class SyncOperation<T> extends RemoteOperation<T> {

    private FileDataStorageManager mStorageManager;

    public FileDataStorageManager getStorageManager() {
        return mStorageManager;
    }


    public RemoteOperationResult<T> execute(FileDataStorageManager storageManager, Context context) {
        if (storageManager == null) {
            throw new IllegalArgumentException("Trying to execute a sync operation with a " +
                    "NULL storage manager");
        }
        if (storageManager.getAccount() == null) {
            throw new IllegalArgumentException("Trying to execute a sync operation with a " +
                    "storage manager for a NULL account");
        }
        mStorageManager = storageManager;
        return super.execute(mStorageManager.getAccount(), context);
    }


    public RemoteOperationResult<T> execute(OwnCloudClient client,
                                            FileDataStorageManager storageManager) {
        if (storageManager == null) {
            throw new IllegalArgumentException("Trying to execute a sync operation with a " +
                    "NULL storage manager");
        }
        mStorageManager = storageManager;
        return super.execute(client);
    }


    public Thread execute(FileDataStorageManager storageManager, Context context,
                          OnRemoteOperationListener listener, Handler listenerHandler) {
        if (storageManager == null) {
            throw new IllegalArgumentException("Trying to execute a sync operation " +
                    "with a NULL storage manager");
        }
        if (storageManager.getAccount() == null) {
            throw new IllegalArgumentException("Trying to execute a sync operation with a" +
                    " storage manager for a NULL account");
        }
        mStorageManager = storageManager;
        return super.execute(mStorageManager.getAccount(), context, listener, listenerHandler);
    }


    public Thread execute(OwnCloudClient client, FileDataStorageManager storageManager,
                          OnRemoteOperationListener listener, Handler listenerHandler) {
        if (storageManager == null) {
            throw new IllegalArgumentException("Trying to execute a sync operation " +
                    "with a NULL storage manager");
        }
        mStorageManager = storageManager;
        return super.execute(client, listener, listenerHandler);
    }
}