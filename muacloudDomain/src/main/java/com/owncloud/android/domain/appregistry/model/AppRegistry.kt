
package com.owncloud.android.domain.appregistry.model

data class AppRegistry(
    val accountName: String,
    val mimetypes: List<AppRegistryMimeType>
)

data class AppRegistryMimeType(
    val mimeType: String,
    val ext: String? = null,
    val appProviders: List<AppRegistryProvider>,
    val name: String? = null,
    val icon: String? = null,
    val description: String? = null,
    val allowCreation: Boolean? = null,
    val defaultApplication: String? = null
)

data class AppRegistryProvider(
    val name: String,
    val icon: String,
)
