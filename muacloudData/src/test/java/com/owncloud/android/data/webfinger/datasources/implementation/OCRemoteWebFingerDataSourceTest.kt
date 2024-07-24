

package com.owncloud.android.data.webfinger.datasources.implementation

import com.owncloud.android.data.ClientManager
import com.owncloud.android.domain.webfinger.model.WebFingerRel
import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.resources.webfinger.services.implementation.OCWebFingerService
import com.owncloud.android.testutil.OC_ACCESS_TOKEN
import com.owncloud.android.testutil.OC_ACCOUNT_ID
import com.owncloud.android.testutil.OC_SECURE_SERVER_INFO_BASIC_AUTH
import com.owncloud.android.utils.createRemoteOperationResultMock
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class OCRemoteWebFingerDataSourceTest {

    private lateinit var ocRemoteWebFingerDatasource: OCRemoteWebFingerDataSource

    private val clientManager: ClientManager = mockk(relaxed = true)
    private val ownCloudClient: OwnCloudClient = mockk(relaxed = true)
    private val ocWebFingerService: OCWebFingerService = mockk()
    private val urls: List<String> = listOf(
        "http://webfinger.owncloud/tests/server-instance1",
        "http://webfinger.owncloud/tests/server-instance2",
        "http://webfinger.owncloud/tests/server-instance3",
    )

    @Before
    fun setUp() {
        ocRemoteWebFingerDatasource = OCRemoteWebFingerDataSource(
            ocWebFingerService,
            clientManager,
        )

        every { clientManager.getClientForAnonymousCredentials(OC_SECURE_SERVER_INFO_BASIC_AUTH.baseUrl, false) } returns ownCloudClient

    }

    @Test
    fun `getInstancesFromWebFinger returns a list of String of web finger urls`() {

        val getInstancesFromWebFingerResult: RemoteOperationResult<List<String>> =
            createRemoteOperationResultMock(data = urls, isSuccess = true)

        every {
            ocWebFingerService.getInstancesFromWebFinger(
                lookupServer = OC_SECURE_SERVER_INFO_BASIC_AUTH.baseUrl,
                resource = OC_SECURE_SERVER_INFO_BASIC_AUTH.baseUrl,
                rel = WebFingerRel.OIDC_ISSUER_DISCOVERY.uri,
                ownCloudClient,
            )
        } returns getInstancesFromWebFingerResult

        val actualResult = ocRemoteWebFingerDatasource.getInstancesFromWebFinger(
            lookupServer = OC_SECURE_SERVER_INFO_BASIC_AUTH.baseUrl,
            rel = WebFingerRel.OIDC_ISSUER_DISCOVERY,
            resource = OC_SECURE_SERVER_INFO_BASIC_AUTH.baseUrl,
        )

        assertEquals(getInstancesFromWebFingerResult.data, actualResult)

        verify(exactly = 1) {
            clientManager.getClientForAnonymousCredentials(any(), false)
            ocWebFingerService.getInstancesFromWebFinger(
                lookupServer = OC_SECURE_SERVER_INFO_BASIC_AUTH.baseUrl,
                resource = OC_SECURE_SERVER_INFO_BASIC_AUTH.baseUrl,
                rel = WebFingerRel.OIDC_ISSUER_DISCOVERY.uri,
                ownCloudClient,
            )
        }
    }

    @Test
    fun `getInstancesFromAuthenticatedWebFinger returns a list of String of web finger urls`() {

        val getInstancesFromAuthenticatedWebFingerResult: RemoteOperationResult<List<String>> =
            createRemoteOperationResultMock(data = urls, isSuccess = true)

        every {
            ocWebFingerService.getInstancesFromWebFinger(
                lookupServer = OC_SECURE_SERVER_INFO_BASIC_AUTH.baseUrl,
                resource = OC_SECURE_SERVER_INFO_BASIC_AUTH.baseUrl,
                rel = WebFingerRel.OIDC_ISSUER_DISCOVERY.uri,
                ownCloudClient,
            )
        } returns getInstancesFromAuthenticatedWebFingerResult

        val actualResult = ocRemoteWebFingerDatasource.getInstancesFromAuthenticatedWebFinger(
            lookupServer = OC_SECURE_SERVER_INFO_BASIC_AUTH.baseUrl,
            rel = WebFingerRel.OIDC_ISSUER_DISCOVERY,
            resource = OC_SECURE_SERVER_INFO_BASIC_AUTH.baseUrl,
            username = OC_ACCOUNT_ID,
            accessToken = OC_ACCESS_TOKEN
        )

        assertEquals(getInstancesFromAuthenticatedWebFingerResult.data, actualResult)

        verify(exactly = 1) {
            ownCloudClient.credentials = any()
            clientManager.getClientForAnonymousCredentials(any(), false)
            ocWebFingerService.getInstancesFromWebFinger(
                lookupServer = OC_SECURE_SERVER_INFO_BASIC_AUTH.baseUrl,
                resource = OC_SECURE_SERVER_INFO_BASIC_AUTH.baseUrl,
                rel = WebFingerRel.OIDC_ISSUER_DISCOVERY.uri,
                any(),
            )
        }
    }
}
