
package com.owncloud.android.domain.files.usecases

import com.owncloud.android.domain.exceptions.UnauthorizedException
import com.owncloud.android.domain.files.FileRepository
import com.owncloud.android.testutil.OC_FOLDER
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import org.junit.Assert
import org.junit.Test

class GetFileByIdUseCaseTest {

    private val repository: FileRepository = spyk()
    private val useCase = GetFileByIdUseCase(repository)
    private val useCaseParams = GetFileByIdUseCase.Params(OC_FOLDER.id!!)

    @Test
    fun `get file by id - ok`() {
        every { repository.getFileById(useCaseParams.fileId) } returns OC_FOLDER

        val useCaseResult = useCase(useCaseParams)

        Assert.assertTrue(useCaseResult.isSuccess)
        Assert.assertEquals(OC_FOLDER, useCaseResult.getDataOrNull())

        verify(exactly = 1) { repository.getFileById(useCaseParams.fileId) }
    }

    @Test
    fun `get file by id - ok - null`() {
        every { repository.getFileById(useCaseParams.fileId) } returns null

        val useCaseResult = useCase(useCaseParams)

        Assert.assertTrue(useCaseResult.isSuccess)
        Assert.assertEquals(null, useCaseResult.getDataOrNull())

        verify(exactly = 1) { repository.getFileById(useCaseParams.fileId) }
    }

    @Test
    fun `get file by id - ko`() {
        every { repository.getFileById(useCaseParams.fileId) } throws UnauthorizedException()

        val useCaseResult = useCase(useCaseParams)

        Assert.assertTrue(useCaseResult.isError)
        Assert.assertTrue(useCaseResult.getThrowableOrNull() is UnauthorizedException)

        verify(exactly = 1) { repository.getFileById(useCaseParams.fileId) }
    }
}
