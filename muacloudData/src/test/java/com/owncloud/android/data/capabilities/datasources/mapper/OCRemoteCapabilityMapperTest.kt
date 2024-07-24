

package com.owncloud.android.data.capabilities.datasources.mapper

import com.owncloud.android.testutil.OC_CAPABILITY
import org.junit.Assert
import org.junit.Test

class OCRemoteCapabilityMapperTest {

    private val ocRemoteCapabilityMapper = RemoteCapabilityMapper()

    @Test
    fun checkToModelNull() {
        Assert.assertNull(ocRemoteCapabilityMapper.toModel(null))
    }

    @Test
    fun checkToModelNotNull() {
        val remoteCapability = ocRemoteCapabilityMapper.toRemote(OC_CAPABILITY)
        Assert.assertNotNull(remoteCapability)

        val capability = ocRemoteCapabilityMapper.toModel(remoteCapability)
        Assert.assertNotNull(capability)
        Assert.assertEquals(capability, OC_CAPABILITY)
    }
}
