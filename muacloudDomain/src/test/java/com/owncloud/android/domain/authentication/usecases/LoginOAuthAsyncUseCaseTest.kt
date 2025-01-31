
package com.owncloud.android.domain.authentication.usecases

import com.owncloud.android.domain.authentication.AuthenticationRepository
import com.owncloud.android.testutil.OC_ACCESS_TOKEN
import com.owncloud.android.testutil.OC_ACCOUNT_NAME
import com.owncloud.android.testutil.OC_AUTH_TOKEN_TYPE
import com.owncloud.android.testutil.OC_REFRESH_TOKEN
import com.owncloud.android.testutil.OC_SCOPE
import com.owncloud.android.testutil.OC_SECURE_SERVER_INFO_BASIC_AUTH
import com.owncloud.android.testutil.oauth.OC_CLIENT_REGISTRATION
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginOAuthAsyncUseCaseTest {

    private val repository: AuthenticationRepository = spyk()
    private val useCase = LoginOAuthAsyncUseCase(repository)
    private val useCaseParams = LoginOAuthAsyncUseCase.Params(
        serverInfo = OC_SECURE_SERVER_INFO_BASIC_AUTH,
        username = "test",
        authTokenType = OC_AUTH_TOKEN_TYPE,
        accessToken = OC_ACCESS_TOKEN,
        refreshToken = OC_REFRESH_TOKEN,
        scope = OC_SCOPE,
        updateAccountWithUsername = null,
        clientRegistrationInfo = OC_CLIENT_REGISTRATION
    )

    @Test
    fun `login oauth - ko - invalid params`() {
        var invalidLoginOAuthUseCaseParams = useCaseParams.copy(serverInfo = null)
        var loginOAuthUseCaseResult = useCase(invalidLoginOAuthUseCaseParams)

        assertTrue(loginOAuthUseCaseResult.isError)
        assertTrue(loginOAuthUseCaseResult.getThrowableOrNull() is IllegalArgumentException)

        invalidLoginOAuthUseCaseParams = useCaseParams.copy(authTokenType = "")
        loginOAuthUseCaseResult = useCase(invalidLoginOAuthUseCaseParams)

        assertTrue(loginOAuthUseCaseResult.isError)
        assertTrue(loginOAuthUseCaseResult.getThrowableOrNull() is IllegalArgumentException)

        invalidLoginOAuthUseCaseParams = useCaseParams.copy(accessToken = "")
        loginOAuthUseCaseResult = useCase(invalidLoginOAuthUseCaseParams)

        assertTrue(loginOAuthUseCaseResult.isError)
        assertTrue(loginOAuthUseCaseResult.getThrowableOrNull() is IllegalArgumentException)

        invalidLoginOAuthUseCaseParams = useCaseParams.copy(refreshToken = "")
        loginOAuthUseCaseResult = useCase(invalidLoginOAuthUseCaseParams)

        assertTrue(loginOAuthUseCaseResult.isError)
        assertTrue(loginOAuthUseCaseResult.getThrowableOrNull() is IllegalArgumentException)

        verify(exactly = 0) { repository.loginOAuth(any(), any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `login oauth - ok`() {
        every { repository.loginOAuth(any(), any(), any(), any(), any(), any(), any(), any()) } returns OC_ACCOUNT_NAME

        val loginOAuthUseCaseResult = useCase(useCaseParams)

        assertTrue(loginOAuthUseCaseResult.isSuccess)
        assertEquals(OC_ACCOUNT_NAME, loginOAuthUseCaseResult.getDataOrNull())

        verify(exactly = 1) { repository.loginOAuth(any(), any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `login oauth - ko - another exception`() {
        every { repository.loginOAuth(any(), any(), any(), any(), any(), any(), any(), any()) } throws Exception()

        val loginOAuthUseCaseResult = useCase(useCaseParams)

        assertTrue(loginOAuthUseCaseResult.isError)
        assertTrue(loginOAuthUseCaseResult.getThrowableOrNull() is Exception)

        verify(exactly = 1) { repository.loginOAuth(any(), any(), any(), any(), any(), any(), any(), any()) }
    }
}
