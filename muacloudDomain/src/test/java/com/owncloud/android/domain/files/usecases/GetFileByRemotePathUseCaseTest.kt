
package com.owncloud.android.domain.files.usecases

import com.owncloud.android.domain.exceptions.UnauthorizedException
import com.owncloud.android.domain.files.FileRepository
import com.owncloud.android.testutil.OC_FOLDER
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import org.junit.Assert
import org.junit.Test

class GetFileByRemotePathUseCaseTest {

    private val repository: FileRepository = spyk()
    private val useCase = GetFileByRemotePathUseCase(repository)
    private val useCaseParams = GetFileByRemotePathUseCase.Params("owner", "remotePath")

    @Test
    fun `get file by remote path - ok`() {
        every { repository.getFileByRemotePath(useCaseParams.remotePath, useCaseParams.owner) } returns OC_FOLDER

        val useCaseResult = useCase(useCaseParams)

        Assert.assertTrue(useCaseResult.isSuccess)

        Assert.assertEquals(OC_FOLDER, useCaseResult.getDataOrNull())

        verify(exactly = 1) { repository.getFileByRemotePath(useCaseParams.remotePath, useCaseParams.owner) }
    }

    @Test
    fun `get file by remote path - ok - null`() {
        every { repository.getFileByRemotePath(useCaseParams.remotePath, useCaseParams.owner) } returns null

        val useCaseResult = useCase(useCaseParams)

        Assert.assertTrue(useCaseResult.isSuccess)
        Assert.assertEquals(null, useCaseResult.getDataOrNull())

        verify(exactly = 1) { repository.getFileByRemotePath(useCaseParams.remotePath, useCaseParams.owner) }
    }

    @Test
    fun `get file by remote path - ko`() {
        every {
            repository.getFileByRemotePath(useCaseParams.remotePath, useCaseParams.owner)
        } throws UnauthorizedException()

        val useCaseResult = useCase(useCaseParams)

        Assert.assertTrue(useCaseResult.isError)
        Assert.assertTrue(useCaseResult.getThrowableOrNull() is UnauthorizedException)

        verify(exactly = 1) { repository.getFileByRemotePath(useCaseParams.remotePath, useCaseParams.owner) }
    }
}
