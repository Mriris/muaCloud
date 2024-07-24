
package com.owncloud.android.domain.authentication.oauth

import com.owncloud.android.domain.exceptions.ServerNotReachableException
import com.owncloud.android.testutil.oauth.OC_TOKEN_REQUEST_REFRESH
import com.owncloud.android.testutil.oauth.OC_TOKEN_RESPONSE
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import org.junit.Assert
import org.junit.Test

class RequestTokenUseCaseTest {

    private val repository: OAuthRepository = spyk()
    private val useCase = RequestTokenUseCase(repository)
    private val useCaseParams = RequestTokenUseCase.Params(OC_TOKEN_REQUEST_REFRESH)

    @Test
    fun `test request token use case - ok`() {
        every { repository.performTokenRequest(useCaseParams.tokenRequest) } returns OC_TOKEN_RESPONSE

        val useCaseResult = useCase(useCaseParams)

        Assert.assertTrue(useCaseResult.isSuccess)
        Assert.assertEquals(OC_TOKEN_RESPONSE, useCaseResult.getDataOrNull())

        verify(exactly = 1) { repository.performTokenRequest(useCaseParams.tokenRequest) }
    }

    @Test
    fun `test request token use case - ko`() {
        every { repository.performTokenRequest(useCaseParams.tokenRequest) } throws ServerNotReachableException()

        val useCaseResult = useCase(useCaseParams)

        Assert.assertTrue(useCaseResult.isError)
        Assert.assertTrue(useCaseResult.getThrowableOrNull() is ServerNotReachableException)

        verify(exactly = 1) { repository.performTokenRequest(useCaseParams.tokenRequest) }
    }
}