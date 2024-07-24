

package com.owncloud.android.domain.spaces.model

import com.owncloud.android.testutil.OC_SPACE_PERSONAL
import com.owncloud.android.testutil.OC_SPACE_PROJECT_DISABLED
import com.owncloud.android.testutil.OC_SPACE_PROJECT_WITHOUT_IMAGE
import com.owncloud.android.testutil.OC_SPACE_PROJECT_WITH_IMAGE
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OCSpaceTest {

    @Test
    fun `test space is personal - ok - true`() {
        val ocSpace = OC_SPACE_PERSONAL
        assertTrue(ocSpace.isPersonal)
    }

    @Test
    fun `test space is personal - ok - false`() {
        val ocSpace = OC_SPACE_PROJECT_WITH_IMAGE
        assertFalse(ocSpace.isPersonal)
    }

    @Test
    fun `test space is project - ok - true`() {
        val ocSpace = OC_SPACE_PROJECT_WITH_IMAGE
        assertTrue(ocSpace.isProject)
    }

    @Test
    fun `test space is project - ok - false`() {
        val ocSpace = OC_SPACE_PERSONAL
        assertFalse(ocSpace.isProject)
    }

    @Test
    fun `test space is disabled - ok - true`() {
        val ocSpace = OC_SPACE_PROJECT_DISABLED
        assertTrue(ocSpace.isDisabled)
    }

    @Test
    fun `test space is disabled - ok - false`() {
        val ocSpace = OC_SPACE_PROJECT_WITH_IMAGE
        assertFalse(ocSpace.isDisabled)
    }

    @Test
    fun `test get space special image - ok - has image`() {
        val ocSpace = OC_SPACE_PROJECT_WITH_IMAGE
        assertNotNull(ocSpace.getSpaceSpecialImage())
    }

    @Test
    fun `test get space special image - ok - does not have image`() {
        val ocSpace = OC_SPACE_PROJECT_WITHOUT_IMAGE
        assertNull(ocSpace.getSpaceSpecialImage())
    }

}
