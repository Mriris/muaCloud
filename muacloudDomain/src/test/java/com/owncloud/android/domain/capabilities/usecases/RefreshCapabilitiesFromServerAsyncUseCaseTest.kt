

package com.owncloud.android.domain.capabilities.usecases

import com.owncloud.android.domain.capabilities.CapabilityRepository
import com.owncloud.android.domain.exceptions.UnauthorizedException
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import org.junit.Assert
import org.junit.Test

class RefreshCapabilitiesFromServerAsyncUseCaseTest {

    private val repository: CapabilityRepository = spyk()
    private val useCase = RefreshCapabilitiesFromServerAsyncUseCase((repository))
    private val useCaseParams = RefreshCapabilitiesFromServerAsyncUseCase.Params("")

    @Test
    fun `refresh capabilities from server - ok`() {
        every { repository.refreshCapabilitiesForAccount(any()) } returns Unit

        val useCaseResult = useCase(useCaseParams)

        Assert.assertTrue(useCaseResult.isSuccess)
        Assert.assertEquals(Unit, useCaseResult.getDataOrNull())

        verify(exactly = 1) { repository.refreshCapabilitiesForAccount("") }
    }

    @Test
    fun `refresh capabilities from server - ko`() {
        every { repository.refreshCapabilitiesForAccount(any()) } throws UnauthorizedException()

        val useCaseResult = useCase(useCaseParams)

        Assert.assertTrue(useCaseResult.isError)
        Assert.assertTrue(useCaseResult.getThrowableOrNull() is UnauthorizedException)

        verify(exactly = 1) { repository.refreshCapabilitiesForAccount("") }
    }
}
