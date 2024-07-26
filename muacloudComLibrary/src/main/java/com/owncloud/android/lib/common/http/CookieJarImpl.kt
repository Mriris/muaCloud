package com.owncloud.android.lib.common.http

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

class CookieJarImpl(
    private val cookieStore: HashMap<String, List<Cookie>>
) : CookieJar {

    fun containsCookieWithName(cookies: List<Cookie>, name: String): Boolean {
        for (cookie: Cookie in cookies) {
            if (cookie.name == name) {
                return true
            }
        }
        return false
    }

    fun getUpdatedCookies(oldCookies: List<Cookie>, newCookies: List<Cookie>): List<Cookie> {
        val updatedList = ArrayList<Cookie>(newCookies)
        for (oldCookie: Cookie in oldCookies) {
            if (!containsCookieWithName(updatedList, oldCookie.name)) {
                updatedList.add(oldCookie)
            }
        }
        return updatedList
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {

        val currentCookies: List<Cookie> = cookieStore[url.host] ?: ArrayList()
        val updatedCookies: List<Cookie> = getUpdatedCookies(currentCookies, cookies)
        cookieStore[url.host] = updatedCookies
    }

    override fun loadForRequest(url: HttpUrl) =
        cookieStore[url.host] ?: ArrayList()
}