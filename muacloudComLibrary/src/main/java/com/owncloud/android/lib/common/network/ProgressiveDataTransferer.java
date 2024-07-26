
package com.owncloud.android.lib.common.network;

import java.util.Collection;

public interface ProgressiveDataTransferer {

    void addDatatransferProgressListener(OnDatatransferProgressListener listener);

    void addDatatransferProgressListeners(Collection<OnDatatransferProgressListener> listeners);

    void removeDatatransferProgressListener(OnDatatransferProgressListener listener);

}
