package com.owncloud.android.lib

import com.owncloud.android.lib.common.http.CookieJarImpl
import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CookieJarImplTest {

    private val oldCookies = listOf(COOKIE_A, COOKIE_B_OLD)
    private val newCookies = listOf(COOKIE_B_NEW)
    private val updatedCookies = listOf(COOKIE_A, COOKIE_B_NEW)
    private val cookieStore = hashMapOf(SOME_HOST to oldCookies)

    private val cookieJarImpl = CookieJarImpl(cookieStore)

    @Test
    fun `contains cookie with name - ok - true`() {
        assertTrue(cookieJarImpl.containsCookieWithName(oldCookies, COOKIE_B_OLD.name))
    }

    @Test
    fun `contains cookie with name - ok - false`() {
        assertFalse(cookieJarImpl.containsCookieWithName(newCookies, COOKIE_A.name))
    }

    @Test
    fun `get updated cookies - ok`() {
        val generatedUpdatedCookies = cookieJarImpl.getUpdatedCookies(oldCookies, newCookies)
        assertEquals(2, generatedUpdatedCookies.size)
        assertEquals(updatedCookies[0], generatedUpdatedCookies[1])
        assertEquals(updatedCookies[1], generatedUpdatedCookies[0])
    }

    @Test
    fun `store cookie via saveFromResponse - ok`() {
        cookieJarImpl.saveFromResponse(SOME_URL, newCookies)
        val generatedUpdatedCookies = cookieStore[SOME_HOST]
        assertEquals(2, generatedUpdatedCookies?.size)
        assertEquals(updatedCookies[0], generatedUpdatedCookies?.get(1))
        assertEquals(updatedCookies[1], generatedUpdatedCookies?.get(0))
    }

    @Test
    fun `load for request - ok`() {
        val cookies = cookieJarImpl.loadForRequest(SOME_URL)
        assertEquals(oldCookies[0], cookies[0])
        assertEquals(oldCookies[1], cookies[1])
    }

    companion object {
        const val SOME_HOST = "some.host.com"
        val SOME_URL = "https://$SOME_HOST".toHttpUrl()
        val COOKIE_A = Cookie.parse(SOME_URL, "CookieA=CookieValueA")!!
        val COOKIE_B_OLD = Cookie.parse(SOME_URL, "CookieB=CookieOldValueB")!!
        val COOKIE_B_NEW = Cookie.parse(SOME_URL, "CookieB=CookieNewValueB")!!
    }
}
