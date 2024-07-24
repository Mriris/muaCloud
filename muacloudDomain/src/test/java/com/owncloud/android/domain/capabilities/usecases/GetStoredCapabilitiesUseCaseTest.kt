

package com.owncloud.android.domain.capabilities.usecases

import com.owncloud.android.domain.capabilities.CapabilityRepository
import com.owncloud.android.testutil.OC_CAPABILITY
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GetStoredCapabilitiesUseCaseTest {

    private val repository: CapabilityRepository = spyk()
    private val useCase = GetStoredCapabilitiesUseCase((repository))
    private val useCaseParams = GetStoredCapabilitiesUseCase.Params("user@server")

    @Test
    fun `get stored capabilities - ok`() {
        every { repository.getStoredCapabilities(any()) } returns OC_CAPABILITY

        val capability = useCase(useCaseParams)

        assertEquals(OC_CAPABILITY, capability)

        verify(exactly = 1) { repository.getStoredCapabilities(any()) }
    }

    @Test
    fun `get stored capabilities - ok - null`() {
        every { repository.getStoredCapabilities(any()) } returns null

        val capability = useCase(useCaseParams)

        assertNull(capability)

        verify(exactly = 1) { repository.getStoredCapabilities(any()) }
    }

    @Test(expected = Exception::class)
    fun `get stored capabilities - ko`() {
        every { repository.getStoredCapabilities(any()) } throws Exception()

        useCase(useCaseParams)
    }
}
