
package com.owncloud.android.domain.user.usecases

import com.owncloud.android.domain.exceptions.UnauthorizedException
import com.owncloud.android.domain.user.UserRepository
import com.owncloud.android.testutil.OC_ACCOUNT_NAME
import com.owncloud.android.testutil.OC_USER_INFO
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetUserInfoAsyncUseCaseTest {

    private val repository: UserRepository = spyk()
    private val useCase = GetUserInfoAsyncUseCase(repository)
    private val useCaseParams = GetUserInfoAsyncUseCase.Params(OC_ACCOUNT_NAME)

    @Test
    fun `get user info - ok`() {
        every { repository.getUserInfo(OC_ACCOUNT_NAME) } returns OC_USER_INFO

        val useCaseResult = useCase(useCaseParams)

        assertTrue(useCaseResult.isSuccess)
        assertEquals(OC_USER_INFO, useCaseResult.getDataOrNull())

        verify(exactly = 1) { repository.getUserInfo(OC_ACCOUNT_NAME) }
    }

    @Test
    fun `get user info - ko`() {
        every { repository.getUserInfo(OC_ACCOUNT_NAME) } throws UnauthorizedException()

        val useCaseResult = useCase(useCaseParams)

        assertTrue(useCaseResult.isError)
        assertTrue(useCaseResult.getThrowableOrNull() is UnauthorizedException)

        verify(exactly = 1) { repository.getUserInfo(OC_ACCOUNT_NAME) }
    }
}
