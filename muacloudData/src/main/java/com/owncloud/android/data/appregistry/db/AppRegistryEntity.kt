
package com.owncloud.android.data.appregistry.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.owncloud.android.data.ProviderMeta
import com.owncloud.android.data.appregistry.db.AppRegistryEntity.Companion.APP_REGISTRY_ACCOUNT_NAME
import com.owncloud.android.data.appregistry.db.AppRegistryEntity.Companion.APP_REGISTRY_MIME_TYPE

@Entity(
    tableName = ProviderMeta.ProviderTableMeta.APP_REGISTRY_TABLE_NAME,
    primaryKeys = [APP_REGISTRY_ACCOUNT_NAME, APP_REGISTRY_MIME_TYPE]
)
data class AppRegistryEntity(
    @ColumnInfo(name = APP_REGISTRY_ACCOUNT_NAME)
    val accountName: String,
    @ColumnInfo(name = APP_REGISTRY_MIME_TYPE)
    val mimeType: String,
    val ext: String? = null,
    @ColumnInfo(name = APP_REGISTRY_APP_PROVIDERS)
    val appProviders: String,
    val name: String? = null,
    val icon: String? = null,
    val description: String? = null,
    @ColumnInfo(name = APP_REGISTRY_ALLOW_CREATION)
    val allowCreation: Boolean? = null,
    @ColumnInfo(name = APP_REGISTRY_DEFAULT_APPLICATION)
    val defaultApplication: String? = null
) {

    companion object {
        const val APP_REGISTRY_MIME_TYPES = "mime_types"
        const val APP_REGISTRY_ACCOUNT_NAME = "account_name"
        const val APP_REGISTRY_MIME_TYPE = "mime_type"
        const val APP_REGISTRY_APP_PROVIDERS = "app_providers"
        const val APP_REGISTRY_ALLOW_CREATION = "allow_creation"
        const val APP_REGISTRY_DEFAULT_APPLICATION = "default_application"
    }
}
