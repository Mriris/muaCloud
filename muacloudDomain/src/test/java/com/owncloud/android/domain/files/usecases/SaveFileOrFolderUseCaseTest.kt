

package com.owncloud.android.domain.files.usecases

import com.owncloud.android.domain.exceptions.UnauthorizedException
import com.owncloud.android.domain.files.FileRepository
import com.owncloud.android.testutil.OC_FILE
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import org.junit.Assert
import org.junit.Test

class SaveFileOrFolderUseCaseTest {
    private val fileRepository: FileRepository = spyk()
    private val useCase = SaveFileOrFolderUseCase(fileRepository)
    private val useCaseParamsFile = SaveFileOrFolderUseCase.Params(OC_FILE)

    @Test
    fun `save file or folder - ok`() {
        val useCaseResult = useCase(useCaseParamsFile)
        Assert.assertTrue(useCaseResult.isSuccess)
        Assert.assertFalse(useCaseResult.isError)
        Assert.assertEquals(Unit, useCaseResult.getDataOrNull())

        verify(exactly = 1) { fileRepository.saveFile(useCaseParamsFile.fileToSave) }
    }

    @Test
    fun `save file or folder - ko`() {
        every { fileRepository.saveFile(any()) } throws UnauthorizedException()

        val useCaseResult = useCase(useCaseParamsFile)

        Assert.assertFalse(useCaseResult.isSuccess)
        Assert.assertTrue(useCaseResult.getThrowableOrNull() is UnauthorizedException)

        verify(exactly = 1) { fileRepository.saveFile(useCaseParamsFile.fileToSave) }
    }
}
