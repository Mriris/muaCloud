
package com.owncloud.android.lib.resources.appregistry.services

import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.resources.appregistry.CreateRemoteFileWithAppProviderOperation
import com.owncloud.android.lib.resources.appregistry.GetRemoteAppRegistryOperation
import com.owncloud.android.lib.resources.appregistry.GetUrlToOpenInWebRemoteOperation
import com.owncloud.android.lib.resources.appregistry.responses.AppRegistryResponse

class OCAppRegistryService(override val client: OwnCloudClient) : AppRegistryService {
    override fun getAppRegistry(appUrl: String?): RemoteOperationResult<AppRegistryResponse> =
        GetRemoteAppRegistryOperation(appUrl).execute(client)

    override fun getUrlToOpenInWeb(openWebEndpoint: String, fileId: String, appName: String): RemoteOperationResult<String> =
        GetUrlToOpenInWebRemoteOperation(
            openWithWebEndpoint = openWebEndpoint,
            fileId = fileId,
            appName = appName
        ).execute(client)

    override fun createFileWithAppProvider(
        createFileWithAppProviderEndpoint: String,
        parentContainerId: String,
        filename: String
    ): RemoteOperationResult<String> =
        CreateRemoteFileWithAppProviderOperation(
            createFileWithAppProviderEndpoint = createFileWithAppProviderEndpoint,
            parentContainerId = parentContainerId,
            filename = filename,
        ).execute(client)
}
