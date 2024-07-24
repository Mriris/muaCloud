

package com.owncloud.android.domain.capabilities.usecases

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.owncloud.android.domain.capabilities.CapabilityRepository
import com.owncloud.android.domain.capabilities.model.OCCapability
import com.owncloud.android.testutil.OC_CAPABILITY
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

class GetCapabilitiesAsLiveDataUseCaseTest {

    @Rule
    @JvmField
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private val repository: CapabilityRepository = spyk()
    private val useCase = GetCapabilitiesAsLiveDataUseCase((repository))
    private val useCaseParams = GetCapabilitiesAsLiveDataUseCase.Params("")

    @Test
    fun `get capabilities as livedata - ok`() {
        val capabilitiesLiveData = MutableLiveData<OCCapability>()
        every { repository.getCapabilitiesAsLiveData(any()) } returns capabilitiesLiveData

        val capabilitiesToEmit = listOf(OC_CAPABILITY)

        val capabilitiesEmitted = mutableListOf<OCCapability>()

        useCase(useCaseParams).observeForever {
            capabilitiesEmitted.add(it!!)
        }

        capabilitiesToEmit.forEach { capabilitiesLiveData.postValue(it) }

        Assert.assertEquals(capabilitiesToEmit, capabilitiesEmitted)

        verify(exactly = 1) { repository.getCapabilitiesAsLiveData(any()) }
    }

    @Test(expected = Exception::class)
    fun `get capabilities as livedata - ko`() {
        every { repository.getCapabilitiesAsLiveData(any()) } throws Exception()

        useCase(useCaseParams)
    }
}
