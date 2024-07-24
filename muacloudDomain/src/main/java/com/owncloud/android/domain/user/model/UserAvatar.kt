
package com.owncloud.android.domain.user.model

data class UserAvatar(
    val avatarData: ByteArray = byteArrayOf(),
    val mimeType: String = "",
    val eTag: String = ""
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserAvatar

        if (!avatarData.contentEquals(other.avatarData)) return false
        if (mimeType != other.mimeType) return false
        if (eTag != other.eTag) return false

        return true
    }

    override fun hashCode(): Int {
        var result = avatarData.contentHashCode()
        result = 31 * result + mimeType.hashCode()
        result = 31 * result + eTag.hashCode()
        return result
    }
}
