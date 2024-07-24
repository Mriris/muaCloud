

package com.owncloud.android.data.sharing.shares.datasources.mapper

import com.owncloud.android.testutil.OC_SHARE
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class RemoteShareMapperTest {

    private val ocRemoteShareMapper = RemoteShareMapper()

    @Test
    fun checkToModelNull() {
        assertNull(ocRemoteShareMapper.toModel(null))
    }

    @Test
    fun checkToModelNotNull() {
        val remoteShare = ocRemoteShareMapper.toRemote(OC_SHARE)
        assertNotNull(remoteShare)

        val share = ocRemoteShareMapper.toModel(remoteShare)
        assertNotNull(share)
        assertEquals(OC_SHARE, share)
    }
}
