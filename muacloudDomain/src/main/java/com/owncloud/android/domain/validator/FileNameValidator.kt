
package com.owncloud.android.domain.validator

import com.owncloud.android.domain.exceptions.validation.FileNameException
import java.util.regex.Pattern

class FileNameValidator {

    @Throws(FileNameException::class)
    fun validateOrThrowException(string: String) {
        if (string.trim().isBlank()) {
            throw FileNameException(type = FileNameException.FileNameExceptionType.FILE_NAME_EMPTY)
        } else if (string.length >= FILE_NAME_MAX_LENGTH_ALLOWED) {
            throw FileNameException(type = FileNameException.FileNameExceptionType.FILE_NAME_TOO_LONG)
        } else if (FILE_NAME_REGEX.containsMatchIn(string)) {
            throw FileNameException(type = FileNameException.FileNameExceptionType.FILE_NAME_FORBIDDEN_CHARACTERS)
        }
    }

    companion object {

        private val FILE_NAME_REGEX = Pattern.compile(".*[/\\\\].*").toRegex()
        private const val FILE_NAME_MAX_LENGTH_ALLOWED = 250
    }
}
