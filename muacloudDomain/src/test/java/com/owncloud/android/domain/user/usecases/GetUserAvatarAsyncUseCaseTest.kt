
package com.owncloud.android.domain.user.usecases

import com.owncloud.android.domain.exceptions.UnauthorizedException
import com.owncloud.android.domain.user.UserRepository
import com.owncloud.android.testutil.OC_ACCOUNT_NAME
import com.owncloud.android.testutil.OC_USER_AVATAR
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetUserAvatarAsyncUseCaseTest {

    private val repository: UserRepository = spyk()
    private val useCase = GetUserAvatarAsyncUseCase(repository)
    private val useCaseParams = GetUserAvatarAsyncUseCase.Params(OC_ACCOUNT_NAME)

    @Test
    fun `get user avatar - ok`() {
        every { repository.getUserAvatar(OC_ACCOUNT_NAME) } returns OC_USER_AVATAR

        val useCaseResult = useCase(useCaseParams)

        assertTrue(useCaseResult.isSuccess)
        assertEquals(OC_USER_AVATAR, useCaseResult.getDataOrNull())

        verify(exactly = 1) { repository.getUserAvatar(OC_ACCOUNT_NAME) }
    }

    @Test
    fun `get user avatar - ko`() {
        every { repository.getUserAvatar(OC_ACCOUNT_NAME) } throws UnauthorizedException()

        val useCaseResult = useCase(useCaseParams)

        assertTrue(useCaseResult.isError)
        assertTrue(useCaseResult.getThrowableOrNull() is UnauthorizedException)

        verify(exactly = 1) { repository.getUserAvatar(OC_ACCOUNT_NAME) }
    }
}
