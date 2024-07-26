

package com.owncloud.android.data.sharing.shares.db

import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class OCShareEntityTest {

    @Test
    fun testEqualsNamedParams() {
        val item1 = OCShareEntity(
            shareType = 0,
            shareWith = "",
            path = "/Photos/image2.jpg",
            permissions = 1,
            sharedDate = 1542628397,
            expirationDate = 0,
            token = "pwdasd12dasdWZ",
            sharedWithDisplayName = "",
            sharedWithAdditionalInfo = "",
            isFolder = false,
            remoteId = "remoteId",
            accountOwner = "admin@server",
            name = "",
            shareLink = ""
        )

        val item2 = OCShareEntity(
            0,
            "",
            "/Photos/image2.jpg",
            1,
            1542628397,
            0,
            "pwdasd12dasdWZ",
            "",
            "",
            false,
            "remoteId",
            "admin@server",
            "",
            ""
        )


        assertTrue(item1 == item2)
        assertFalse(item1 === item2)
    }

    @Test
    fun testEqualsNamedParamsNullValues() {
        val item1 = OCShareEntity(
            shareType = 0,
            shareWith = null,
            path = "/Photos/image2.jpg",
            permissions = 1,
            sharedDate = 1542628397,
            expirationDate = 0,
            token = null,
            sharedWithDisplayName = null,
            sharedWithAdditionalInfo = null,
            isFolder = false,
            remoteId = "remoteId",
            accountOwner = "admin@server",
            name = null,
            shareLink = null
        )

        val item2 = OCShareEntity(
            0,
            null,
            "/Photos/image2.jpg",
            1,
            1542628397,
            0,
            null,
            null,
            null,
            false,
            "remoteId",
            "admin@server",
            null,
            null
        )

        assertTrue(item1 == item2)
        assertFalse(item1 === item2)
    }

    @Test
    fun testNotEqualsNamedParams() {
        val item1 = OCShareEntity(
            shareType = 0,
            shareWith = "",
            path = "/Photos/image2.jpg",
            permissions = 1,
            sharedDate = 1542628397,
            expirationDate = 0,
            token = "pwdasd12dasdWZ",
            sharedWithDisplayName = "",
            sharedWithAdditionalInfo = "",
            isFolder = false,
            remoteId = "remoteId",
            accountOwner = "admin@server",
            name = "",
            shareLink = ""
        )

        val item2 = OCShareEntity(
            0,
            "",
            "/Photos/image2.jpg",
            1,
            1542628397,
            0,
            "pwdasd12dasdWZ",
            "",
            "",
            false,
            "remoteId",
            "AnyServer",
            "",
            ""
        )

        assertFalse(item1 == item2)
        assertFalse(item1 === item2)
    }
}
