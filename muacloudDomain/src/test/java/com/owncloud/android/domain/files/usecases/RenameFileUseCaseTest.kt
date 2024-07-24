
package com.owncloud.android.domain.files.usecases

import com.owncloud.android.domain.exceptions.UnauthorizedException
import com.owncloud.android.domain.files.FileRepository
import com.owncloud.android.testutil.OC_FILE
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RenameFileUseCaseTest {
    private val repository: FileRepository = spyk()
    private val setLastUsageFileUseCase: SetLastUsageFileUseCase = mockk(relaxed = true)
    private val useCase = RenameFileUseCase(repository, setLastUsageFileUseCase)
    private val useCaseParams = RenameFileUseCase.Params(OC_FILE, "Video.mp4")

    @Test
    fun `rename file - ok`() {
        every { repository.renameFile(any(), any()) } returns Unit

        val useCaseResult = useCase(useCaseParams)

        assertTrue(useCaseResult.isSuccess)
        assertEquals(Unit, useCaseResult.getDataOrNull())

        verify(exactly = 1) { repository.renameFile(any(), useCaseParams.newName) }
    }

    @Test
    fun `rename file - ko - other exception`() {
        every { repository.renameFile(any(), any()) } throws UnauthorizedException()

        val useCaseResult = useCase(useCaseParams)

        assertTrue(useCaseResult.isError)
        assertTrue(useCaseResult.getThrowableOrNull() is UnauthorizedException)

        verify(exactly = 1) { repository.renameFile(any(), any()) }
    }
}
