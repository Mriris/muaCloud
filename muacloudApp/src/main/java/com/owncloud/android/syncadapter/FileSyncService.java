
package com.owncloud.android.syncadapter;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


public class FileSyncService extends Service {

    private static FileSyncAdapter sSyncAdapter = null;

    private static final Object sSyncAdapterLock = new Object();

        @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = new FileSyncAdapter(getApplicationContext(), true);
            }
        }
    }

        @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }

}
