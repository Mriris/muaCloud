
package com.owncloud.android.syncadapter;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


public class FileSyncService extends Service {

    // Storage for an instance of the sync adapter
    private static FileSyncAdapter sSyncAdapter = null;
    // Object to use as a thread-safe lock
    private static final Object sSyncAdapterLock = new Object();

    /*
     * {@inheritDoc}
     */
    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = new FileSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    /*
     * {@inheritDoc}
     */
    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }

}
