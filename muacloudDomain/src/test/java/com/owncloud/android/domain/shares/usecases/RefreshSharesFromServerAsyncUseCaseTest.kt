

package com.owncloud.android.domain.shares.usecases

import com.owncloud.android.domain.exceptions.UnauthorizedException
import com.owncloud.android.domain.sharing.shares.ShareRepository
import com.owncloud.android.domain.sharing.shares.usecases.RefreshSharesFromServerAsyncUseCase
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RefreshSharesFromServerAsyncUseCaseTest {

    private val shareRepository: ShareRepository = spyk()
    private val useCase = RefreshSharesFromServerAsyncUseCase((shareRepository))
    private val useCaseParams = RefreshSharesFromServerAsyncUseCase.Params("", "")

    @Test
    fun `refresh shares from server - ok`() {
        val useCaseResult = useCase(useCaseParams)

        assertTrue(useCaseResult.isSuccess)
        assertEquals(Unit, useCaseResult.getDataOrNull())

        verify(exactly = 1) { shareRepository.refreshSharesFromNetwork("", "") }
    }

    @Test
    fun `refresh shares from server - ko`() {
        every { shareRepository.refreshSharesFromNetwork(any(), any()) } throws UnauthorizedException()

        val useCaseResult = useCase(useCaseParams)

        assertTrue(useCaseResult.isError)
        assertTrue(useCaseResult.getThrowableOrNull() is UnauthorizedException)

        verify(exactly = 1) { shareRepository.refreshSharesFromNetwork("", "") }
    }
}
