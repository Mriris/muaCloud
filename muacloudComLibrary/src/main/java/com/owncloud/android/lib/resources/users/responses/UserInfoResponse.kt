package com.owncloud.android.lib.resources.users.responses

import com.owncloud.android.lib.resources.users.RemoteUserInfo
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserInfoResponse(
    val id: String,
    @Json(name = "display-name")
    val displayName: String,
    val email: String?
) {
    fun toRemoteUserInfo() = RemoteUserInfo(
        id = id,
        displayName = displayName,
        email = email
    )
}
