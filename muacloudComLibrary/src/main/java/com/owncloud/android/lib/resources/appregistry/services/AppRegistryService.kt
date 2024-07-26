
package com.owncloud.android.lib.resources.appregistry.services

import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.resources.Service
import com.owncloud.android.lib.resources.appregistry.responses.AppRegistryResponse

interface AppRegistryService : Service {
    fun getAppRegistry(appUrl: String?): RemoteOperationResult<AppRegistryResponse>

    fun getUrlToOpenInWeb(
        openWebEndpoint: String,
        fileId: String,
        appName: String,
    ): RemoteOperationResult<String>

    fun createFileWithAppProvider(
        createFileWithAppProviderEndpoint: String,
        parentContainerId: String,
        filename: String,
    ): RemoteOperationResult<String>
}
