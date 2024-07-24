

package com.owncloud.android.data.appregistry.datasources

import com.owncloud.android.domain.appregistry.model.AppRegistry
import com.owncloud.android.domain.appregistry.model.AppRegistryMimeType
import kotlinx.coroutines.flow.Flow

interface LocalAppRegistryDataSource {
    fun getAppRegistryForMimeTypeAsStream(
        accountName: String,
        mimeType: String,
    ): Flow<AppRegistryMimeType?>

    fun getAppRegistryWhichAllowCreation(
        accountName: String,
    ): Flow<List<AppRegistryMimeType>>

    fun saveAppRegistryForAccount(
        appRegistry: AppRegistry
    )

    fun deleteAppRegistryForAccount(
        accountName: String,
    )
}
