

package com.owncloud.android.domain.shares.usecases

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.owncloud.android.domain.sharing.shares.ShareRepository
import com.owncloud.android.domain.sharing.shares.model.OCShare
import com.owncloud.android.domain.sharing.shares.usecases.GetSharesAsLiveDataUseCase
import com.owncloud.android.testutil.OC_SHARE
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class GetSharesAsLiveDataUseCaseTest {

    @Rule
    @JvmField
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private val repository: ShareRepository = spyk()
    private val useCase = GetSharesAsLiveDataUseCase(repository)
    private val useCaseParams = GetSharesAsLiveDataUseCase.Params("", "")

    @Test
    fun `get shares as livedata - ok`() {
        val sharesLiveData = MutableLiveData<List<OCShare>>()
        every { repository.getSharesAsLiveData(any(), any()) } returns sharesLiveData

        val sharesToEmit = listOf(
            OC_SHARE,
            OC_SHARE.copy(id = 2),
            OC_SHARE.copy(id = 3)
        )

        val sharesEmitted = mutableListOf<OCShare>()

        useCase(useCaseParams).observeForever {
            it?.forEach { ocShare -> sharesEmitted.add(ocShare) }
        }

        sharesLiveData.postValue(sharesToEmit)

        assertEquals(sharesToEmit, sharesEmitted)

        verify(exactly = 1) { repository.getSharesAsLiveData(any(), any()) }
    }

    @Test(expected = Exception::class)
    fun `get shares as livedata - ko`() {
        every { repository.getSharesAsLiveData(any(), any()) } throws Exception()

        useCase(useCaseParams)
    }
}
