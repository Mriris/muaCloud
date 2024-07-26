package com.owncloud.android.lib.resources.shares.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class ShareeOcsResponse(
    val exact: ExactSharees?,
    val groups: List<ShareeItem>,
    val remotes: List<ShareeItem>,
    val users: List<ShareeItem>
) {
    fun getFlatRepresentationWithoutExact() = ArrayList<ShareeItem>().apply {
        addAll(users)
        addAll(remotes)
        addAll(groups)
    }
}

@JsonClass(generateAdapter = true)
data class ExactSharees(
    val groups: List<ShareeItem>,
    val remotes: List<ShareeItem>,
    val users: List<ShareeItem>
) {
    fun getFlatRepresentation() = ArrayList<ShareeItem>().apply {
        addAll(users)
        addAll(remotes)
        addAll(groups)
    }
}

@JsonClass(generateAdapter = true)
data class ShareeItem(
    val label: String,
    val value: ShareeValue
)

@JsonClass(generateAdapter = true)
data class ShareeValue(
    val shareType: Int,
    val shareWith: String,
    @Json(name = "shareWithAdditionalInfo")
    val additionalInfo: String?
)
