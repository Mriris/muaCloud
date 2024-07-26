
package com.owncloud.android.lib

import com.owncloud.android.lib.resources.status.StatusRequester
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StatusRequesterTest {
    private val requester = StatusRequester()

    @Test
    fun `update location - ok - absolute path`() {
        val newLocation = requester.updateLocationWithRedirectPath(TEST_DOMAIN, "$TEST_DOMAIN$SUB_PATH")
        assertEquals("$TEST_DOMAIN$SUB_PATH", newLocation)
    }

    @Test
    fun `update location - ok - smaller absolute path`() {
        val newLocation = requester.updateLocationWithRedirectPath("$TEST_DOMAIN$SUB_PATH", TEST_DOMAIN)
        assertEquals(TEST_DOMAIN, newLocation)
    }

    @Test
    fun `update location - ok - relative path`() {
        val newLocation = requester.updateLocationWithRedirectPath(TEST_DOMAIN, SUB_PATH)
        assertEquals("$TEST_DOMAIN$SUB_PATH", newLocation)
    }

    @Test
    fun `update location - ok - replace relative path`() {
        val newLocation = requester.updateLocationWithRedirectPath("$TEST_DOMAIN/some/other/subdir", SUB_PATH)
        assertEquals("$TEST_DOMAIN$SUB_PATH", newLocation)
    }

    @Test
    fun `check redirect to unsecure connection - ok - redirect to http`() {
        assertTrue(
            requester.isRedirectedToNonSecureConnection(false, SECURE_DOMAIN, UNSECURE_DOMAIN
            )
        )
    }

    @Test
    fun `check redirect to unsecure connection - ko - redirect to https from http`() {
        assertFalse(
            requester.isRedirectedToNonSecureConnection(false, UNSECURE_DOMAIN, SECURE_DOMAIN
            )
        )
    }

    @Test
    fun `check redirect to unsecure connection - ko - from https to https`() {
        assertFalse(
            requester.isRedirectedToNonSecureConnection(false, SECURE_DOMAIN, SECURE_DOMAIN)
        )
    }

    @Test
    fun `check redirect to unsecure connection - ok - from https to https with previous http`() {
        assertTrue(
            requester.isRedirectedToNonSecureConnection(true, SECURE_DOMAIN, SECURE_DOMAIN)
        )
    }

    @Test
    fun `check redirect to unsecure connection - ok - from http to http`() {
        assertFalse(
            requester.isRedirectedToNonSecureConnection(false, UNSECURE_DOMAIN, UNSECURE_DOMAIN)
        )
    }

    companion object {
        const val TEST_DOMAIN = "https://cloud.somewhere.com"
        const val SUB_PATH = "/subdir"

        const val SECURE_DOMAIN = "https://cloud.somewhere.com"
        const val UNSECURE_DOMAIN = "http://somewhereelse.org"
    }
}
