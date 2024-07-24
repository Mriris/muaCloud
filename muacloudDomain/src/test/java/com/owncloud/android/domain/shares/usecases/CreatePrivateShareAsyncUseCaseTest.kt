
package com.owncloud.android.domain.shares.usecases

import com.owncloud.android.domain.exceptions.UnauthorizedException
import com.owncloud.android.domain.sharing.shares.ShareRepository
import com.owncloud.android.domain.sharing.shares.model.ShareType
import com.owncloud.android.domain.sharing.shares.usecases.CreatePrivateShareAsyncUseCase
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CreatePrivateShareAsyncUseCaseTest {

    private val repository: ShareRepository = spyk()
    private val useCase = CreatePrivateShareAsyncUseCase(repository)
    private val useCaseParams = CreatePrivateShareAsyncUseCase.Params("", ShareType.USER, "", 1, "")

    @Test
    fun `create private share - ok`() {
        every {
            repository.insertPrivateShare(any(), any(), any(), any(), any())
        } returns Unit

        val useCaseResult = useCase(useCaseParams)

        assertTrue(useCaseResult.isSuccess)
        assertEquals(Unit, useCaseResult.getDataOrNull())

        verify(exactly = 1) { repository.insertPrivateShare("", ShareType.USER, "", 1, "") }
    }

    @Test
    fun `create private share - ko - unauthorized exception`() {
        every { repository.insertPrivateShare(any(), any(), any(), any(), any()) } throws UnauthorizedException()

        val useCaseResult = useCase(useCaseParams)

        assertTrue(useCaseResult.isError)
        assertTrue(useCaseResult.getThrowableOrNull() is UnauthorizedException)

        verify(exactly = 1) { repository.insertPrivateShare("", ShareType.USER, "", 1, "") }
    }

    @Test
    fun `create private share - ko - illegal argument exception`() {
        val useCaseParamsNotValid1 = useCaseParams.copy(shareType = null)
        val useCaseResult1 = useCase(useCaseParamsNotValid1)

        assertTrue(useCaseResult1.isError)
        assertTrue(useCaseResult1.getThrowableOrNull() is IllegalArgumentException)

        val useCaseParamsNotValid2 = useCaseParams.copy(shareType = ShareType.CONTACT)
        val useCaseResult2 = useCase(useCaseParamsNotValid2)

        assertTrue(useCaseResult2.isError)
        assertTrue(useCaseResult2.getThrowableOrNull() is IllegalArgumentException)

        verify(exactly = 0) { repository.insertPrivateShare(any(), any(), any(), any(), any()) }
    }
}
