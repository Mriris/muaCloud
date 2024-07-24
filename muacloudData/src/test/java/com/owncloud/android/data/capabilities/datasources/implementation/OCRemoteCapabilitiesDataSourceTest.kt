

package com.owncloud.android.data.capabilities.datasources.implementation

import com.owncloud.android.data.ClientManager
import com.owncloud.android.data.capabilities.datasources.mapper.RemoteCapabilityMapper
import com.owncloud.android.lib.resources.status.services.implementation.OCCapabilityService
import com.owncloud.android.testutil.OC_ACCOUNT_NAME
import com.owncloud.android.testutil.OC_CAPABILITY
import com.owncloud.android.utils.createRemoteOperationResultMock
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class OCRemoteCapabilitiesDataSourceTest {

    private lateinit var ocRemoteCapabilitiesDataSource: OCRemoteCapabilitiesDataSource

    private val ocCapabilityService: OCCapabilityService = mockk()
    private val clientManager: ClientManager = mockk(relaxed = true)
    private val remoteCapabilityMapper = RemoteCapabilityMapper()

    @Before
    fun setUp() {
        every { clientManager.getCapabilityService(OC_ACCOUNT_NAME) } returns ocCapabilityService

        ocRemoteCapabilitiesDataSource =
            OCRemoteCapabilitiesDataSource(
                clientManager,
                remoteCapabilityMapper
            )
    }

    @Test
    fun `getCapabilities returns a OCCapability`() {
        val remoteCapability = remoteCapabilityMapper.toRemote(OC_CAPABILITY)!!

        val getRemoteCapabilitiesOperationResult = createRemoteOperationResultMock(remoteCapability, true)

        every { ocCapabilityService.getCapabilities() } returns getRemoteCapabilitiesOperationResult

        val result = ocRemoteCapabilitiesDataSource.getCapabilities(OC_ACCOUNT_NAME)

        assertEquals(OC_CAPABILITY, result)

        verify(exactly = 1) {
            clientManager.getCapabilityService(OC_ACCOUNT_NAME)
            ocCapabilityService.getCapabilities()
        }
    }
}
