

package com.owncloud.android.domain.validator

import com.owncloud.android.domain.exceptions.validation.FileNameException
import com.owncloud.android.domain.exceptions.validation.FileNameException.FileNameExceptionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FileNameValidatorTest {

    private val validator = FileNameValidator()

    @Test
    fun `validate name - ok`() {
        val result = runCatching { validator.validateOrThrowException("Photos") }
        assertEquals(Unit, result.getOrNull())
    }

    @Test
    fun `validate name - ko - empty`() {
        val result = runCatching { validator.validateOrThrowException("    ") }

        validateExceptionAndType(result, FileNameExceptionType.FILE_NAME_EMPTY)
    }

    @Test
    fun `validate name - ko - back slash`() {
        val result = runCatching { validator.validateOrThrowException("/Photos") }

        validateExceptionAndType(result, FileNameExceptionType.FILE_NAME_FORBIDDEN_CHARACTERS)
    }

    @Test
    fun `validate name - ko - forward slash`() {
        val result = runCatching { validator.validateOrThrowException("\\Photos") }

        validateExceptionAndType(result, FileNameExceptionType.FILE_NAME_FORBIDDEN_CHARACTERS)
    }

    @Test
    fun `validate name - ko - both slashes()`() {
        val result = runCatching { validator.validateOrThrowException("\\Photos/") }

        validateExceptionAndType(result, FileNameExceptionType.FILE_NAME_FORBIDDEN_CHARACTERS)
    }
}

private fun validateExceptionAndType(
    result: Result<Unit>,
    type: FileNameExceptionType
) {
    with(result.exceptionOrNull()) {
        assertTrue(this is FileNameException)
        assertEquals(type, (this as FileNameException).type)
    }
}
