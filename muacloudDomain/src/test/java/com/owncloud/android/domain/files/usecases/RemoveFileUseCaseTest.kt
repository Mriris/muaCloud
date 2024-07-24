
package com.owncloud.android.domain.files.usecases

import com.owncloud.android.domain.exceptions.UnauthorizedException
import com.owncloud.android.domain.files.FileRepository
import com.owncloud.android.testutil.OC_FILE
import com.owncloud.android.testutil.OC_FOLDER
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RemoveFileUseCaseTest {
    private val repository: FileRepository = spyk()
    private val useCase = RemoveFileUseCase(repository)
    private val useCaseParams = RemoveFileUseCase.Params(listOf(OC_FILE, OC_FOLDER), removeOnlyLocalCopy = true)

    @Test
    fun `remove file - ok`() {
        every { repository.deleteFiles(any(), any()) } returns Unit

        val useCaseResult = useCase(useCaseParams.copy(removeOnlyLocalCopy = false))

        assertTrue(useCaseResult.isSuccess)
        assertEquals(Unit, useCaseResult.getDataOrNull())

        verify(exactly = 1) { repository.deleteFiles(any(), removeOnlyLocalCopy = false) }
    }

    @Test
    fun `remove file - ok - remove local only`() {
        every { repository.deleteFiles(any(), any()) } returns Unit

        val useCaseResult = useCase(useCaseParams)

        assertTrue(useCaseResult.isSuccess)
        assertEquals(Unit, useCaseResult.getDataOrNull())

        verify(exactly = 1) { repository.deleteFiles(any(), removeOnlyLocalCopy = true) }
    }

    @Test
    fun `remove file - ko - empty list`() {
        val useCaseResult = useCase(useCaseParams.copy(listOfFilesToDelete = listOf()))

        assertTrue(useCaseResult.isError)
        assertTrue(useCaseResult.getThrowableOrNull() is IllegalArgumentException)

        verify(exactly = 0) { repository.deleteFiles(any(), removeOnlyLocalCopy = true) }
    }

    @Test
    fun `remove file - ko - other exception`() {
        every { repository.deleteFiles(any(), any()) } throws UnauthorizedException()

        val useCaseResult = useCase(useCaseParams)

        assertTrue(useCaseResult.isError)
        assertTrue(useCaseResult.getThrowableOrNull() is UnauthorizedException)

        verify(exactly = 1) { repository.deleteFiles(any(), any()) }
    }
}
