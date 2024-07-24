

package com.owncloud.android.domain.shares.usecases

import com.owncloud.android.domain.exceptions.UnauthorizedException
import com.owncloud.android.domain.sharing.shares.ShareRepository
import com.owncloud.android.domain.sharing.shares.usecases.DeleteShareAsyncUseCase
import com.owncloud.android.testutil.OC_SHARE
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeleteShareAsyncUseCaseTest {

    private val repository: ShareRepository = spyk()
    private val useCase = DeleteShareAsyncUseCase(repository)
    private val useCaseParams = DeleteShareAsyncUseCase.Params(OC_SHARE.remoteId, OC_SHARE.accountOwner)

    @Test
    fun `delete share - ok`() {
        every { repository.deleteShare(any(), any()) } returns Unit

        val useCaseResult = useCase(useCaseParams)

        assertTrue(useCaseResult.isSuccess)
        assertEquals(Unit, useCaseResult.getDataOrNull())

        verify(exactly = 1) { repository.deleteShare(OC_SHARE.remoteId, OC_SHARE.accountOwner) }
    }

    @Test
    fun `delete share - ko`() {
        every { repository.deleteShare(any(), any()) } throws UnauthorizedException()

        val useCaseResult = useCase(useCaseParams)

        assertTrue(useCaseResult.isError)
        assertTrue(useCaseResult.getThrowableOrNull() is UnauthorizedException)

        verify(exactly = 1) { repository.deleteShare(OC_SHARE.remoteId, OC_SHARE.accountOwner) }
    }
}
