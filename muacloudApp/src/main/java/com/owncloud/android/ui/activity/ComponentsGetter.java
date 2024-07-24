

package com.owncloud.android.ui.activity;

import com.owncloud.android.datamodel.FileDataStorageManager;
import com.owncloud.android.services.OperationsService.OperationsServiceBinder;
import com.owncloud.android.ui.helpers.FileOperationsHelper;

public interface ComponentsGetter {

    OperationsServiceBinder getOperationsServiceBinder();

    FileDataStorageManager getStorageManager();

    FileOperationsHelper getFileOperationsHelper();

}
