

package com.owncloud.android.data.oauth.datasources.implementation

import com.owncloud.android.data.ClientManager
import com.owncloud.android.data.oauth.OC_REMOTE_CLIENT_REGISTRATION_RESPONSE
import com.owncloud.android.data.oauth.OC_REMOTE_OIDC_DISCOVERY_RESPONSE
import com.owncloud.android.data.oauth.OC_REMOTE_TOKEN_RESPONSE
import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.resources.oauth.responses.ClientRegistrationResponse
import com.owncloud.android.lib.resources.oauth.responses.OIDCDiscoveryResponse
import com.owncloud.android.lib.resources.oauth.responses.TokenResponse
import com.owncloud.android.lib.resources.oauth.services.OIDCService
import com.owncloud.android.testutil.OC_SECURE_BASE_URL
import com.owncloud.android.testutil.oauth.OC_CLIENT_REGISTRATION
import com.owncloud.android.testutil.oauth.OC_CLIENT_REGISTRATION_REQUEST
import com.owncloud.android.testutil.oauth.OC_OIDC_SERVER_CONFIGURATION
import com.owncloud.android.testutil.oauth.OC_TOKEN_REQUEST_ACCESS
import com.owncloud.android.testutil.oauth.OC_TOKEN_RESPONSE
import com.owncloud.android.utils.createRemoteOperationResultMock
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class OCRemoteOAuthDataSourceTest {
    private lateinit var remoteOAuthDataSource: OCRemoteOAuthDataSource

    private val clientManager: ClientManager = mockk(relaxed = true)
    private val ocClientMocked: OwnCloudClient = mockk()

    private val oidcService: OIDCService = mockk()

    @Before
    fun setUp() {
        every { clientManager.getClientForAnonymousCredentials(any(), any()) } returns ocClientMocked

        remoteOAuthDataSource = OCRemoteOAuthDataSource(
            clientManager = clientManager,
            oidcService = oidcService,
        )
    }

    @Test
    fun `performOIDCDiscovery returns a OIDCServerConfiguration`() {
        val oidcDiscoveryResult: RemoteOperationResult<OIDCDiscoveryResponse> =
            createRemoteOperationResultMock(data = OC_REMOTE_OIDC_DISCOVERY_RESPONSE, isSuccess = true)

        every {
            oidcService.getOIDCServerDiscovery(ocClientMocked)
        } returns oidcDiscoveryResult

        val oidcDiscovery = remoteOAuthDataSource.performOIDCDiscovery(OC_SECURE_BASE_URL)

        assertNotNull(oidcDiscovery)
        assertEquals(OC_OIDC_SERVER_CONFIGURATION, oidcDiscovery)

        verify(exactly = 1) {
            clientManager.getClientForAnonymousCredentials(OC_SECURE_BASE_URL, false)
            oidcService.getOIDCServerDiscovery(ocClientMocked)
        }
    }

    @Test
    fun `performTokenRequest returns a TokenResponse`() {
        val tokenResponseResult: RemoteOperationResult<TokenResponse> =
            createRemoteOperationResultMock(data = OC_REMOTE_TOKEN_RESPONSE, isSuccess = true)

        every {
            oidcService.performTokenRequest(ocClientMocked, any())
        } returns tokenResponseResult

        val tokenResponse = remoteOAuthDataSource.performTokenRequest(OC_TOKEN_REQUEST_ACCESS)

        assertNotNull(tokenResponse)
        assertEquals(OC_TOKEN_RESPONSE, tokenResponse)

        verify(exactly = 1) {
            clientManager.getClientForAnonymousCredentials(OC_SECURE_BASE_URL, any())
            oidcService.performTokenRequest(ocClientMocked, any())
        }
    }

    @Test
    fun `registerClient returns a ClientRegistrationInfo`() {
        val clientRegistrationResponse: RemoteOperationResult<ClientRegistrationResponse> =
            createRemoteOperationResultMock(data = OC_REMOTE_CLIENT_REGISTRATION_RESPONSE, isSuccess = true)

        every {
            oidcService.registerClientWithRegistrationEndpoint(ocClientMocked, any())
        } returns clientRegistrationResponse

        val clientRegistrationInfo = remoteOAuthDataSource.registerClient(OC_CLIENT_REGISTRATION_REQUEST)

        assertNotNull(clientRegistrationInfo)
        assertEquals(OC_CLIENT_REGISTRATION, clientRegistrationInfo)

        verify(exactly = 1) {
            clientManager.getClientForAnonymousCredentials(OC_CLIENT_REGISTRATION_REQUEST.registrationEndpoint, false)
            oidcService.registerClientWithRegistrationEndpoint(ocClientMocked, any())
        }
    }
}
