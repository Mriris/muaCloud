

package com.owncloud.android.domain.appregistry

import com.owncloud.android.domain.appregistry.model.AppRegistryMimeType
import kotlinx.coroutines.flow.Flow

interface AppRegistryRepository {
    fun refreshAppRegistryForAccount(accountName: String)

    fun getAppRegistryForMimeTypeAsStream(accountName: String, mimeType: String): Flow<AppRegistryMimeType?>

    fun getAppRegistryWhichAllowCreation(accountName: String): Flow<List<AppRegistryMimeType>>

    fun getUrlToOpenInWeb(accountName: String, openWebEndpoint: String, fileId: String, appName: String): String

    fun createFileWithAppProvider(accountName: String, createFileWithAppProviderEndpoint: String, parentContainerId: String, filename: String): String
}
