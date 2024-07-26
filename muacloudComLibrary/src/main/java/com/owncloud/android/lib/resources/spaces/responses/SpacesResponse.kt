package com.owncloud.android.lib.resources.spaces.responses

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SpacesResponseWrapper(
    val value: List<SpaceResponse>
)

@JsonClass(generateAdapter = true)
data class SpaceResponse(
    val description: String?,
    val driveAlias: String,
    val driveType: String,
    val id: String,
    val lastModifiedDateTime: String?,
    val name: String,
    val owner: OwnerResponse?,
    val quota: QuotaResponse?,
    val root: RootResponse,
    val special: List<SpecialResponse>?,
    val webUrl: String,
)

@JsonClass(generateAdapter = true)
data class OwnerResponse(
    val user: UserResponse
)

@JsonClass(generateAdapter = true)
data class QuotaResponse(
    val remaining: Long?,
    val state: String?,
    val total: Long,
    val used: Long?,
)

@JsonClass(generateAdapter = true)
data class RootResponse(
    val eTag: String?,
    val id: String,
    val webDavUrl: String,
    val deleted: DeleteResponse?,
)

@JsonClass(generateAdapter = true)
data class SpecialResponse(
    val eTag: String,
    val file: FileResponse,
    val id: String,
    val lastModifiedDateTime: String,
    val name: String,
    val size: Int,
    val specialFolder: SpecialFolderResponse,
    val webDavUrl: String
)

@JsonClass(generateAdapter = true)
data class UserResponse(
    val id: String
)

@JsonClass(generateAdapter = true)
data class FileResponse(
    val mimeType: String
)

@JsonClass(generateAdapter = true)
data class DeleteResponse(
    val state: String,
)

@JsonClass(generateAdapter = true)
data class SpecialFolderResponse(
    val name: String
)
