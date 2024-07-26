package com.owncloud.android.lib.resources.appregistry.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AppRegistryResponse(
    @Json(name = "mime-types")
    val value: List<AppRegistryMimeTypeResponse>
)

@JsonClass(generateAdapter = true)
data class AppRegistryMimeTypeResponse(
    @Json(name = "mime_type") val mimeType: String,
    val ext: String? = null,
    @Json(name = "app_providers")
    val appProviders: List<AppRegistryProviderResponse>,
    val name: String? = null,
    val icon: String? = null,
    val description: String? = null,
    @Json(name = "allow_creation")
    val allowCreation: Boolean? = null,
    @Json(name = "default_application")
    val defaultApplication: String? = null
)

@JsonClass(generateAdapter = true)
data class AppRegistryProviderResponse(
    val name: String,
    val icon: String,
)
