
package com.owncloud.android.domain.user.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserInfoTest {
    @Test
    fun testConstructor() {
        val item = UserInfo(
            "admin",
            "adminOc",
            "admin@owncloud.com"
        )

        assertEquals("admin", item.id)
        assertEquals("adminOc", item.displayName)
        assertEquals("admin@owncloud.com", item.email)
    }

    @Test
    fun testEqualsOk() {
        val item1 = UserInfo(
            id = "admin",
            displayName = "adminOc",
            email = null
        )

        val item2 = UserInfo(
            "admin",
            "adminOc",
            null
        )

        assertTrue(item1 == item2)
        assertFalse(item1 === item2)
    }

    @Test
    fun testEqualsKo() {
        val item1 = UserInfo(
            id = "admin",
            displayName = "adminOc",
            email = null
        )

        val item2 = UserInfo(
            "admin",
            "adminOc",
            "demo@owncloud.com"
        )

        assertFalse(item1 == item2)
        assertFalse(item1 === item2)
    }
}
