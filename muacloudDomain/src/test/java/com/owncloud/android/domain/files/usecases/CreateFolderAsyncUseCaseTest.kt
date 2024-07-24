
package com.owncloud.android.domain.files.usecases

import com.owncloud.android.domain.exceptions.UnauthorizedException
import com.owncloud.android.domain.exceptions.validation.FileNameException
import com.owncloud.android.domain.exceptions.validation.FileNameException.FileNameExceptionType.FILE_NAME_EMPTY
import com.owncloud.android.domain.exceptions.validation.FileNameException.FileNameExceptionType.FILE_NAME_FORBIDDEN_CHARACTERS
import com.owncloud.android.domain.files.FileRepository
import com.owncloud.android.testutil.OC_FOLDER
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CreateFolderAsyncUseCaseTest {
    private val repository: FileRepository = spyk()
    private val useCase = CreateFolderAsyncUseCase(repository)
    private val useCaseParams = CreateFolderAsyncUseCase.Params("new folder", OC_FOLDER)

    @Test
    fun `create folder - ok`() {
        every { repository.createFolder(any(), any()) } returns Unit

        val useCaseResult = useCase(useCaseParams)

        assertTrue(useCaseResult.isSuccess)
        assertEquals(Unit, useCaseResult.getDataOrNull())

        verify(exactly = 1) { repository.createFolder(any(), useCaseParams.parentFile) }
    }

    @Test
    fun `create folder - ko - empty name`() {
        val useCaseResult = useCase(useCaseParams.copy(folderName = "   "))

        assertTrue(useCaseResult.isError)
        assertEquals(
            FileNameException(type = FILE_NAME_EMPTY),
            useCaseResult.getThrowableOrNull()
        )
    }

    @Test
    fun `create folder - ko - forbidden chars`() {
        val useCaseResult = useCase(useCaseParams.copy(folderName = "/Photos"))

        assertTrue(useCaseResult.isError)
        assertEquals(
            FileNameException(type = FILE_NAME_FORBIDDEN_CHARACTERS),
            useCaseResult.getThrowableOrNull()
        )
    }

    @Test
    fun `create folder - ko - other exception`() {
        every { repository.createFolder(any(), any()) } throws UnauthorizedException()

        val useCaseResult = useCase(useCaseParams)

        assertTrue(useCaseResult.isError)
        assertTrue(useCaseResult.getThrowableOrNull() is UnauthorizedException)

        verify(exactly = 1) { repository.createFolder(any(), useCaseParams.parentFile) }
    }
}
