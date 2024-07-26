package com.owncloud.android.lib.common.http.methods.webdav

import at.bitfire.dav4jvm.DavOCResource
import okhttp3.Response
import java.net.URL


class MoveMethod(
    url: URL,
    private val destinationUrl: String,
    val forceOverride: Boolean = false
) : DavMethod(url) {
    @Throws(Exception::class)
    override fun onDavExecute(davResource: DavOCResource): Int {
        davResource.move(
            destinationUrl,
            forceOverride,
            super.getRequestHeadersAsHashMap()
        ) { callBackResponse: Response ->
            response = callBackResponse
        }
        return super.statusCode
    }

}
