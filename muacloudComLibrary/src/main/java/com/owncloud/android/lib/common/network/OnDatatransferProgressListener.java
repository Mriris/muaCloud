
package com.owncloud.android.lib.common.network;

public interface OnDatatransferProgressListener {
    void onTransferProgress(long read, long transferred, long percent, String absolutePath);
}