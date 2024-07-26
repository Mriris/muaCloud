package com.owncloud.android.lib.resources.users

data class RemoteAvatarData(
    val avatarData: ByteArray = byteArrayOf(),
    val mimeType: String = "",
    val eTag: String = ""
)
