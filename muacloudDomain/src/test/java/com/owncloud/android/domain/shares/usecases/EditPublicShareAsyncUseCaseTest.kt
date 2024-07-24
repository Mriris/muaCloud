

package com.owncloud.android.domain.shares.usecases

import com.owncloud.android.domain.exceptions.UnauthorizedException
import com.owncloud.android.domain.sharing.shares.ShareRepository
import com.owncloud.android.domain.sharing.shares.usecases.EditPublicShareAsyncUseCase
import com.owncloud.android.testutil.OC_SHARE
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EditPublicShareAsyncUseCaseTest {
    private val repository: ShareRepository = spyk()
    private val useCase = EditPublicShareAsyncUseCase(repository)
    private val useCaseParams = EditPublicShareAsyncUseCase.Params(
        OC_SHARE.remoteId,
        "",
        "",
        OC_SHARE.expirationDate,
        OC_SHARE.permissions,
        OC_SHARE.accountOwner
    )

    @Test
    fun `edit public share - ok`() {
        every {
            repository.updatePublicShare(any(), any(), any(), any(), any(), any())
        } returns Unit

        val useCaseResult = useCase(useCaseParams)

        assertTrue(useCaseResult.isSuccess)
        assertEquals(Unit, useCaseResult.getDataOrNull())

        verify(exactly = 1) {
            repository.updatePublicShare(
                remoteId = OC_SHARE.remoteId,
                name = "",
                password = "",
                expirationDateInMillis = OC_SHARE.expirationDate,
                permissions = OC_SHARE.permissions,
                accountName = OC_SHARE.accountOwner
            )
        }
    }

    @Test
    fun `edit public share - ko`() {
        every {
            repository.updatePublicShare(any(), any(), any(), any(), any(), any())
        } throws UnauthorizedException()

        val useCaseResult = useCase(useCaseParams)

        assertTrue(useCaseResult.isError)
        assertTrue(useCaseResult.getThrowableOrNull() is UnauthorizedException)

        verify(exactly = 1) {
            repository.updatePublicShare(
                remoteId = OC_SHARE.remoteId,
                name = "",
                password = "",
                expirationDateInMillis = OC_SHARE.expirationDate,
                permissions = OC_SHARE.permissions,
                accountName = OC_SHARE.accountOwner
            )
        }
    }
}
