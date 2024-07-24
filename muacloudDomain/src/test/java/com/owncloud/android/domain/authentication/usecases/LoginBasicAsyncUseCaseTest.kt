
package com.owncloud.android.domain.authentication.usecases

import com.owncloud.android.domain.authentication.AuthenticationRepository
import com.owncloud.android.testutil.OC_ACCOUNT_NAME
import com.owncloud.android.testutil.OC_SECURE_SERVER_INFO_BASIC_AUTH
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginBasicAsyncUseCaseTest {

    private val repository: AuthenticationRepository = spyk()
    private val useCase = LoginBasicAsyncUseCase(repository)
    private val useCaseParams = LoginBasicAsyncUseCase.Params(
        serverInfo = OC_SECURE_SERVER_INFO_BASIC_AUTH,
        username = "test",
        password = "test",
        updateAccountWithUsername = null
    )

    @Test
    fun `login basic - ko - invalid params`() {
        var invalidLoginBasicUseCaseParams = useCaseParams.copy(serverInfo = null)
        var loginBasicUseCaseResult = useCase(invalidLoginBasicUseCaseParams)

        assertTrue(loginBasicUseCaseResult.isError)
        assertTrue(loginBasicUseCaseResult.getThrowableOrNull() is IllegalArgumentException)

        invalidLoginBasicUseCaseParams = useCaseParams.copy(username = "")
        loginBasicUseCaseResult = useCase(invalidLoginBasicUseCaseParams)

        assertTrue(loginBasicUseCaseResult.isError)
        assertTrue(loginBasicUseCaseResult.getThrowableOrNull() is IllegalArgumentException)

        invalidLoginBasicUseCaseParams = useCaseParams.copy(password = "")
        loginBasicUseCaseResult = useCase(invalidLoginBasicUseCaseParams)

        assertTrue(loginBasicUseCaseResult.isError)
        assertTrue(loginBasicUseCaseResult.getThrowableOrNull() is IllegalArgumentException)

        verify(exactly = 0) { repository.loginBasic(any(), any(), any(), any()) }
    }

    @Test
    fun `login basic - ok`() {
        every { repository.loginBasic(any(), any(), any(), any()) } returns OC_ACCOUNT_NAME

        val loginBasicUseCaseResult = useCase(useCaseParams)

        assertTrue(loginBasicUseCaseResult.isSuccess)
        assertEquals(OC_ACCOUNT_NAME, loginBasicUseCaseResult.getDataOrNull())

        verify(exactly = 1) { repository.loginBasic(any(), any(), any(), any()) }
    }

    @Test
    fun `login basic - ko - another exception`() {
        every { repository.loginBasic(any(), any(), any(), any()) } throws Exception()

        val loginBasicUseCaseResult = useCase(useCaseParams)

        assertTrue(loginBasicUseCaseResult.isError)
        assertTrue(loginBasicUseCaseResult.getThrowableOrNull() is Exception)

        verify(exactly = 1) { repository.loginBasic(any(), any(), any(), any()) }
    }
}
