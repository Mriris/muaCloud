
package com.owncloud.android.domain.user.usecases

import com.owncloud.android.domain.exceptions.UnauthorizedException
import com.owncloud.android.domain.user.UserRepository
import com.owncloud.android.testutil.OC_ACCOUNT_NAME
import com.owncloud.android.testutil.OC_USER_QUOTA
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RefreshUserQuotaFromServerAsyncUseCaseTest {

    private val repository: UserRepository = spyk()
    private val useCase = RefreshUserQuotaFromServerAsyncUseCase(repository)
    private val useCaseParams = RefreshUserQuotaFromServerAsyncUseCase.Params(accountName = OC_ACCOUNT_NAME)

    @Test
    fun `refresh user quota - ok`() {
        every { repository.getUserQuota(OC_ACCOUNT_NAME) } returns OC_USER_QUOTA

        val useCaseResult = useCase(useCaseParams)

        assertTrue(useCaseResult.isSuccess)
        assertEquals(OC_USER_QUOTA, useCaseResult.getDataOrNull())

        verify(exactly = 1) { repository.getUserQuota(OC_ACCOUNT_NAME) }
    }

    @Test
    fun `refresh user quota - ko`() {
        every { repository.getUserQuota(OC_ACCOUNT_NAME) } throws UnauthorizedException()

        val useCaseResult = useCase(useCaseParams)

        assertTrue(useCaseResult.isError)
        assertTrue(useCaseResult.getThrowableOrNull() is UnauthorizedException)

        verify(exactly = 1) { repository.getUserQuota(OC_ACCOUNT_NAME) }
    }
}
