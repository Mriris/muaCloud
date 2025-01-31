
package com.owncloud.android.lib.common.http.methods

import com.owncloud.android.lib.common.http.HttpClient
import okhttp3.Call
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.InputStream
import java.net.MalformedURLException
import java.net.URL
import java.util.concurrent.TimeUnit

abstract class HttpBaseMethod(url: URL) {
    var httpUrl: HttpUrl = url.toHttpUrlOrNull() ?: throw MalformedURLException()
    var request: Request
    var followPermanentRedirects = false
    abstract var response: Response
    var call: Call? = null

    var followRedirects: Boolean = true
    var retryOnConnectionFailure: Boolean = true
    var connectionTimeoutVal: Long? = null
    var connectionTimeoutUnit: TimeUnit? = null
    var readTimeoutVal: Long? = null
        private set
    var readTimeoutUnit: TimeUnit? = null
        private set

    init {
        request = Request.Builder()
            .url(httpUrl)
            .build()
    }

    @Throws(Exception::class)
    open fun execute(httpClient: HttpClient): Int {
        val okHttpClient = httpClient.okHttpClient.newBuilder().apply {
            retryOnConnectionFailure(retryOnConnectionFailure)
            followRedirects(followRedirects)
            readTimeoutUnit?.let { unit ->
                readTimeoutVal?.let { readTimeout(it, unit) }
            }
            connectionTimeoutUnit?.let { unit ->
               connectionTimeoutVal?.let { connectTimeout(it, unit) }
            }
        }.build()

        return onExecute(okHttpClient)
    }

    open fun setUrl(url: HttpUrl) {
        request = request.newBuilder()
            .url(url)
            .build()
    }

    
    fun getRequestHeader(name: String): String? {
        return request.header(name)
    }

    fun getRequestHeadersAsHashMap(): HashMap<String, String?> {
        val headers: HashMap<String, String?> = HashMap()
        val superHeaders: Set<String> = request.headers.names()
        superHeaders.forEach {
            headers[it] = getRequestHeader(it)
        }
        return headers
    }

    open fun addRequestHeader(name: String, value: String) {
        request = request.newBuilder()
            .addHeader(name, value)
            .build()
    }


    open fun setRequestHeader(name: String, value: String) {
        request = request.newBuilder()
            .header(name, value)
            .build()
    }

        val statusCode: Int
        get() = response.code

    val statusMessage: String
        get() = response.message

    open fun getResponseHeaders(): Headers? {
        return response.headers
    }

    open fun getResponseHeader(headerName: String): String? {
        return response.header(headerName)
    }

    fun getResponseBodyAsString(): String = response.peekBody(Long.MAX_VALUE).string()

    open fun getResponseBodyAsStream(): InputStream? {
        return response.body?.byteStream()
    }


    open fun getFinalUrl() = response.request.url






    open fun setReadTimeout(readTimeout: Long, timeUnit: TimeUnit) {
        readTimeoutVal = readTimeout
        readTimeoutUnit = timeUnit
    }

    open fun setConnectionTimeout(
        connectionTimeout: Long,
        timeUnit: TimeUnit
    ) {
        connectionTimeoutVal = connectionTimeout
        connectionTimeoutUnit = timeUnit
    }

        open fun abort() {
        call?.cancel()
    }

    open val isAborted: Boolean
        get() = call?.isCanceled() ?: false



    @Throws(Exception::class)
    protected abstract fun onExecute(okHttpClient: OkHttpClient): Int
}
