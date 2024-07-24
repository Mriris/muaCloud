

package com.owncloud.android.data.appregistry.datasources

import com.owncloud.android.domain.appregistry.model.AppRegistry

interface RemoteAppRegistryDataSource {
    fun getAppRegistryForAccount(
        accountName: String,
        appUrl: String?,
    ): AppRegistry

    fun getUrlToOpenInWeb(
        accountName: String,
        openWebEndpoint: String,
        fileId: String,
        appName: String,
    ): String

    fun createFileWithAppProvider(
        accountName: String,
        createFileWithAppProviderEndpoint: String,
        parentContainerId: String,
        filename: String,
    ): String
}
