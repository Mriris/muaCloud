

package com.owncloud.android.domain.exceptions.validation

class FileNameException(
    val type: FileNameExceptionType
) : Exception() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FileNameException

        return this.type == other.type
    }

    override fun hashCode(): Int = type.hashCode()

    enum class FileNameExceptionType {
        FILE_NAME_EMPTY, FILE_NAME_FORBIDDEN_CHARACTERS, FILE_NAME_TOO_LONG
    }
}
