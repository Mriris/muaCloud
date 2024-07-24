

package com.owncloud.android.data.user.db

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserQuotaEntityTest {
    @Test
    fun testConstructor() {
        val item = UserQuotaEntity(
            "accountName",
            200,
            800
        )

        assertEquals("accountName", item.accountName)
        assertEquals(800, item.available)
        assertEquals(200, item.used)
    }

    @Test
    fun testEqualsOk() {
        val item1 = UserQuotaEntity(
            accountName = "accountName",
            available = 200,
            used = 800
        )

        val item2 = UserQuotaEntity(
            "accountName",
            800,
            200
        )

        assertTrue(item1 == item2)
        assertFalse(item1 === item2)
    }

    @Test
    fun testEqualsKo() {
        val item1 = UserQuotaEntity(
            accountName = "accountName",
            available = 800,
            used = 200
        )

        val item2 = UserQuotaEntity(
            "accountName2",
            200,
            800
        )

        assertFalse(item1 == item2)
        assertFalse(item1 === item2)
    }
}
