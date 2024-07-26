package com.owncloud.android.lib.resources

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CommonOcsResponse<T>(
    val ocs: OCSResponse<T>
)

@JsonClass(generateAdapter = true)
data class OCSResponse<T>(
    val meta: MetaData,
    val data: T?
)

@JsonClass(generateAdapter = true)
data class MetaData(
    val status: String,
    @Json(name = "statuscode")
    val statusCode: Int,
    val message: String?,
    @Json(name = "itemsperpage")
    val itemsPerPage: String?,
    @Json(name = "totalitems")
    val totalItems: String?
)
