package com.owncloud.android.lib.common.http.methods.webdav

import at.bitfire.dav4jvm.Dav4jvm.log
import at.bitfire.dav4jvm.DavOCResource
import at.bitfire.dav4jvm.exception.HttpException
import at.bitfire.dav4jvm.exception.RedirectException
import com.owncloud.android.lib.common.http.HttpConstants
import com.owncloud.android.lib.common.http.methods.HttpBaseMethod
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.net.URL


abstract class DavMethod protected constructor(url: URL) : HttpBaseMethod(url) {
    override lateinit var response: Response
    private var davResource: DavOCResource? = null

    override fun abort() {
        davResource?.cancelCall()
    }

    protected abstract fun onDavExecute(davResource: DavOCResource): Int

    @Throws(Exception::class)
    override fun onExecute(okHttpClient: OkHttpClient): Int {
        return try {
             davResource = DavOCResource(
                okHttpClient.newBuilder().followRedirects(false).build(),
                httpUrl,
                log
            )

            onDavExecute(davResource!!)
        } catch (httpException: HttpException) {

            if (httpException is RedirectException) {
                response = Response.Builder()
                    .header(
                        HttpConstants.LOCATION_HEADER, httpException.redirectLocation
                    )
                    .code(httpException.code)
                    .request(request)
                    .message(httpException.message ?: "")
                    .protocol(Protocol.HTTP_1_1)
                    .build()
            } else {


                if (response.body?.contentType() != null) {
                    val responseBody = (httpException.responseBody ?: "").toResponseBody(response.body?.contentType())
                    response = response.newBuilder()
                        .body(responseBody)
                        .build()
                }
            }
            httpException.code
        }
    }





    override val isAborted: Boolean
        get() = davResource?.isCallAborted() ?: false

}
