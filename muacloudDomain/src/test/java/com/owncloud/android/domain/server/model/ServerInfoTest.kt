

package com.owncloud.android.domain.server.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ServerInfoTest {

    @Test
    fun testConstructor() {
        val item = ServerInfo.BasicServer(
            "10.3.2.1",
            "https://demo.owncloud.com"
        )

        assertEquals("https://demo.owncloud.com", item.baseUrl)
        assertEquals("10.3.2.1", item.ownCloudVersion)
        assertTrue(item.isSecureConnection)
    }

    @Test
    fun testEqualsOk() {
        val item1 = ServerInfo.BasicServer(
            baseUrl = "https://demo.owncloud.com",
            ownCloudVersion = "10.3.2.1",
        )

        val item2 = ServerInfo.BasicServer(
            "10.3.2.1",
            "https://demo.owncloud.com",
        )

        assertTrue(item1 == item2)
        assertFalse(item1 === item2)
    }

    @Test
    fun testEqualsKo() {
        val item1 = ServerInfo.BasicServer(
            baseUrl = "https://demo.owncloud.com",
            ownCloudVersion = "10.3.2.1",
        )

        val item2 = ServerInfo.BasicServer(
            "10.0.0.0",
            "https://demo.owncloud.com",
        )

        assertFalse(item1 == item2)
        assertFalse(item1 === item2)
    }
}
