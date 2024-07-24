

package com.owncloud.android.domain.shares.usecases

import com.owncloud.android.domain.exceptions.UnauthorizedException
import com.owncloud.android.domain.sharing.shares.ShareRepository
import com.owncloud.android.domain.sharing.shares.usecases.CreatePublicShareAsyncUseCase
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CreatePublicShareAsyncUseCaseTest {

    private val repository: ShareRepository = spyk()
    private val useCase = CreatePublicShareAsyncUseCase(repository)
    private val useCaseParams = CreatePublicShareAsyncUseCase.Params("", 1, "", "", 100,  "")

    @Test
    fun `create public share - ok`() {
        every {
            repository.insertPublicShare(any(), any(), any(), any(), any(), any())
        } returns Unit

        val useCaseResult = useCase(useCaseParams)

        assertTrue(useCaseResult.isSuccess)
        assertEquals(Unit, useCaseResult.getDataOrNull())

        verify(exactly = 1) { repository.insertPublicShare("", 1, "", "", 100,  "") }
    }

    @Test
    fun `create public share - ko`() {
        every {
            repository.insertPublicShare(any(), any(), any(), any(), any(), any())
        } throws UnauthorizedException()

        val useCaseResult = useCase(useCaseParams)

        assertTrue(useCaseResult.isError)
        assertTrue(useCaseResult.getThrowableOrNull() is UnauthorizedException)

        verify(exactly = 1) { repository.insertPublicShare("", 1, "", "", 100, "") }
    }

    @Test
    fun `create public share - ko - illegal argument exception`() {
        every {
            repository.insertPublicShare(any(), any(), any(), any(), any(), any())
        } throws IllegalArgumentException()

        val useCaseResult = useCase(useCaseParams)

        assertTrue(useCaseResult.isError)
        assertTrue(useCaseResult.getThrowableOrNull() is IllegalArgumentException)

        verify(exactly = 1) { repository.insertPublicShare("", 1, "", "", 100, "") }
    }
}
