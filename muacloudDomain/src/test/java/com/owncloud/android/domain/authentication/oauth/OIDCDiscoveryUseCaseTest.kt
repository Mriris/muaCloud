
package com.owncloud.android.domain.authentication.oauth

import com.owncloud.android.domain.exceptions.ServerNotReachableException
import com.owncloud.android.testutil.OC_SECURE_SERVER_INFO_BASIC_AUTH
import com.owncloud.android.testutil.oauth.OC_OIDC_SERVER_CONFIGURATION
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import org.junit.Assert
import org.junit.Test

class OIDCDiscoveryUseCaseTest {

    private val repository: OAuthRepository = spyk()
    private val useCase = OIDCDiscoveryUseCase(repository)
    private val useCaseParams = OIDCDiscoveryUseCase.Params(OC_SECURE_SERVER_INFO_BASIC_AUTH.baseUrl)

    @Test
    fun `test perform oidc discovery - ok`() {
        every { repository.performOIDCDiscovery(useCaseParams.baseUrl) } returns OC_OIDC_SERVER_CONFIGURATION

        val useCaseResult = useCase(useCaseParams)

        Assert.assertTrue(useCaseResult.isSuccess)
        Assert.assertEquals(OC_OIDC_SERVER_CONFIGURATION, useCaseResult.getDataOrNull())

        verify(exactly = 1) { repository.performOIDCDiscovery(useCaseParams.baseUrl) }
    }

    @Test
    fun `test perform oidc discovery - ko`() {
        every { repository.performOIDCDiscovery(useCaseParams.baseUrl) } throws ServerNotReachableException()

        val useCaseResult = useCase(useCaseParams)

        Assert.assertTrue(useCaseResult.isError)
        Assert.assertTrue(useCaseResult.getThrowableOrNull() is ServerNotReachableException)

        verify(exactly = 1) { repository.performOIDCDiscovery(useCaseParams.baseUrl) }
    }
}
