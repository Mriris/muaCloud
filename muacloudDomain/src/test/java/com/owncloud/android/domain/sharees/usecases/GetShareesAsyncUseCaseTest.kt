

package com.owncloud.android.domain.sharees.usecases

import com.owncloud.android.domain.exceptions.NoConnectionWithServerException
import com.owncloud.android.domain.sharing.sharees.GetShareesAsyncUseCase
import com.owncloud.android.domain.sharing.sharees.ShareeRepository
import com.owncloud.android.testutil.OC_ACCOUNT_NAME
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import org.junit.Assert.assertTrue
import org.junit.Test

class GetShareesAsyncUseCaseTest {

    private val repository: ShareeRepository = spyk()
    private val useCase = GetShareesAsyncUseCase(repository)
    private val useCaseParams = GetShareesAsyncUseCase.Params("user", 1, 5, OC_ACCOUNT_NAME)

    @Test
    fun `get sharees from server - ok`() {
        every { repository.getSharees(any(), any(), any(), any()) } returns arrayListOf()

        val useCaseResult = useCase(useCaseParams)

        assertTrue(useCaseResult.isSuccess)

        verify(exactly = 1) {
            repository.getSharees("user", 1, 5, OC_ACCOUNT_NAME)
        }
    }

    @Test
    fun `get sharees from server - ko`() {
        every { repository.getSharees(any(), any(), any(), any()) } throws NoConnectionWithServerException()

        val useCaseResult = useCase(useCaseParams)

        assertTrue(useCaseResult.isError)
        assertTrue(useCaseResult.getThrowableOrNull() is NoConnectionWithServerException)

        verify(exactly = 1) {
            repository.getSharees("user", 1, 5, OC_ACCOUNT_NAME)
        }
    }
}
