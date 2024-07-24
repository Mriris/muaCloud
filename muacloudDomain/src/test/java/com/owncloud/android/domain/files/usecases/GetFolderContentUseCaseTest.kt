
package com.owncloud.android.domain.files.usecases

import com.owncloud.android.domain.exceptions.UnauthorizedException
import com.owncloud.android.domain.files.FileRepository
import com.owncloud.android.testutil.OC_FILE
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetFolderContentUseCaseTest {

    private val repository: FileRepository = spyk()
    private val useCase = GetFolderContentUseCase(repository)
    private val useCaseParams = GetFolderContentUseCase.Params(OC_FILE.parentId!!)

    @Test
    fun `get folder content - ok`() {
        every { repository.getFolderContent(useCaseParams.folderId) } returns listOf(OC_FILE)

        val useCaseResult = useCase(useCaseParams)

        assertTrue(useCaseResult.isSuccess)
        assertEquals(listOf(OC_FILE), useCaseResult.getDataOrNull())

        verify(exactly = 1) { repository.getFolderContent(useCaseParams.folderId) }
    }

    @Test
    fun `get folder content - ko`() {
        every { repository.getFolderContent(useCaseParams.folderId) } throws UnauthorizedException()

        val useCaseResult = useCase(useCaseParams)

        assertTrue(useCaseResult.isError)
        assertTrue(useCaseResult.getThrowableOrNull() is UnauthorizedException)

        verify(exactly = 1) { repository.getFolderContent(useCaseParams.folderId) }
    }
}
