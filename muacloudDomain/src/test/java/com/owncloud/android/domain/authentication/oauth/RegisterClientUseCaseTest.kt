
package com.owncloud.android.domain.authentication.oauth

import com.owncloud.android.domain.exceptions.ServerNotReachableException
import com.owncloud.android.testutil.oauth.OC_CLIENT_REGISTRATION
import com.owncloud.android.testutil.oauth.OC_CLIENT_REGISTRATION_REQUEST
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import org.junit.Assert
import org.junit.Test

class RegisterClientUseCaseTest {

    private val repository: OAuthRepository = spyk()
    private val useCase = RegisterClientUseCase(repository)
    private val useCaseParams = RegisterClientUseCase.Params(OC_CLIENT_REGISTRATION_REQUEST)

    @Test
    fun `test register client - ok`() {
        every { repository.registerClient(useCaseParams.clientRegistrationRequest) } returns OC_CLIENT_REGISTRATION

        val useCaseResult = useCase(useCaseParams)

        Assert.assertTrue(useCaseResult.isSuccess)
        Assert.assertEquals(OC_CLIENT_REGISTRATION, useCaseResult.getDataOrNull())

        verify(exactly = 1) { repository.registerClient(useCaseParams.clientRegistrationRequest) }
    }

    @Test
    fun `test register client - ko`() {
        every { repository.registerClient(useCaseParams.clientRegistrationRequest) } throws ServerNotReachableException()

        val useCaseResult = useCase(useCaseParams)

        Assert.assertTrue(useCaseResult.isError)
        Assert.assertTrue(useCaseResult.getThrowableOrNull() is ServerNotReachableException)

        verify(exactly = 1) { repository.registerClient(useCaseParams.clientRegistrationRequest) }
    }
}