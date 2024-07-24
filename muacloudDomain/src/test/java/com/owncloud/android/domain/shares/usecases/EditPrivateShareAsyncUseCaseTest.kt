
package com.owncloud.android.domain.shares.usecases

import com.owncloud.android.domain.exceptions.UnauthorizedException
import com.owncloud.android.domain.sharing.shares.ShareRepository
import com.owncloud.android.domain.sharing.shares.usecases.EditPrivateShareAsyncUseCase
import com.owncloud.android.testutil.OC_SHARE
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EditPrivateShareAsyncUseCaseTest {

    private val repository: ShareRepository = spyk()
    private val useCase = EditPrivateShareAsyncUseCase(repository)
    private val useCaseParams =
        EditPrivateShareAsyncUseCase.Params(OC_SHARE.remoteId, OC_SHARE.permissions, OC_SHARE.accountOwner)

    @Test
    fun `edit private share - ok`() {
        every { repository.updatePrivateShare(any(), any(), any()) } returns Unit

        val useCaseResult = useCase(useCaseParams)

        assertTrue(useCaseResult.isSuccess)
        assertEquals(Unit, useCaseResult.getDataOrNull())

        verify(exactly = 1) {
            repository.updatePrivateShare(
                OC_SHARE.remoteId,
                OC_SHARE.permissions,
                OC_SHARE.accountOwner
            )
        }
    }

    @Test
    fun `edit private share - ko`() {
        every { repository.updatePrivateShare(any(), any(), any()) } throws UnauthorizedException()

        val useCaseResult = useCase(useCaseParams)

        assertTrue(useCaseResult.isError)
        assertTrue(useCaseResult.getThrowableOrNull() is UnauthorizedException)

        verify(exactly = 1) {
            repository.updatePrivateShare(
                OC_SHARE.remoteId,
                OC_SHARE.permissions,
                OC_SHARE.accountOwner
            )
        }
    }
}
