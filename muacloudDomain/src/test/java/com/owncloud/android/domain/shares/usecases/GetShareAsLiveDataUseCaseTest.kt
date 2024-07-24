

package com.owncloud.android.domain.shares.usecases

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.owncloud.android.domain.sharing.shares.ShareRepository
import com.owncloud.android.domain.sharing.shares.model.OCShare
import com.owncloud.android.domain.sharing.shares.usecases.GetShareAsLiveDataUseCase
import com.owncloud.android.testutil.OC_SHARE
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class GetShareAsLiveDataUseCaseTest {

    @Rule
    @JvmField
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private val repository: ShareRepository = spyk()
    private val useCase = GetShareAsLiveDataUseCase(repository)
    private val useCaseParams = GetShareAsLiveDataUseCase.Params(OC_SHARE.remoteId)

    @Test
    fun `get share as livedata - ok`() {
        val shareLiveData = MutableLiveData<OCShare>()
        every { repository.getShareAsLiveData(any()) } returns shareLiveData

        val shareToEmit = listOf(OC_SHARE)

        val shareEmitted = mutableListOf<OCShare>()

        useCase(useCaseParams).observeForever {
            shareEmitted.add(it)
        }

        shareToEmit.forEach { shareLiveData.postValue(it) }

        assertEquals(shareToEmit, shareEmitted)

        verify(exactly = 1) { repository.getShareAsLiveData(OC_SHARE.remoteId) }
    }

    @Test(expected = Exception::class)
    fun `get share as livedata - ko`() {
        every { repository.getShareAsLiveData(any()) } throws Exception()

        useCase(useCaseParams)
    }
}
