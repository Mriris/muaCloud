

package com.owncloud.android.data.appregistry.repository

import com.owncloud.android.data.appregistry.datasources.LocalAppRegistryDataSource
import com.owncloud.android.data.appregistry.datasources.RemoteAppRegistryDataSource
import com.owncloud.android.data.capabilities.datasources.LocalCapabilitiesDataSource
import com.owncloud.android.domain.appregistry.AppRegistryRepository
import com.owncloud.android.domain.appregistry.model.AppRegistryMimeType
import kotlinx.coroutines.flow.Flow

class OCAppRegistryRepository(
    private val localAppRegistryDataSource: LocalAppRegistryDataSource,
    private val remoteAppRegistryDataSource: RemoteAppRegistryDataSource,
    private val localCapabilitiesDataSource: LocalCapabilitiesDataSource,
) : AppRegistryRepository {
    override fun refreshAppRegistryForAccount(accountName: String) {
        val capabilities = localCapabilitiesDataSource.getCapabilitiesForAccount(accountName)
        val appUrl = capabilities?.filesAppProviders?.appsUrl?.substring(1)
        remoteAppRegistryDataSource.getAppRegistryForAccount(accountName, appUrl).also {
            localAppRegistryDataSource.saveAppRegistryForAccount(it)
        }
    }

    override fun getAppRegistryForMimeTypeAsStream(accountName: String, mimeType: String): Flow<AppRegistryMimeType?> =
        localAppRegistryDataSource.getAppRegistryForMimeTypeAsStream(accountName, mimeType)

    override fun getAppRegistryWhichAllowCreation(accountName: String): Flow<List<AppRegistryMimeType>> =
        localAppRegistryDataSource.getAppRegistryWhichAllowCreation(accountName)

    override fun getUrlToOpenInWeb(accountName: String, openWebEndpoint: String, fileId: String, appName: String): String =
        remoteAppRegistryDataSource.getUrlToOpenInWeb(
            accountName = accountName,
            openWebEndpoint = openWebEndpoint,
            fileId = fileId,
            appName = appName,
        )

    override fun createFileWithAppProvider(
        accountName: String,
        createFileWithAppProviderEndpoint: String,
        parentContainerId: String,
        filename: String,
    ): String =
        remoteAppRegistryDataSource.createFileWithAppProvider(
            accountName = accountName,
            createFileWithAppProviderEndpoint = createFileWithAppProviderEndpoint,
            parentContainerId = parentContainerId,
            filename = filename,
        )
}
