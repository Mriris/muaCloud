package com.owncloud.android.lib.common.http.methods.webdav

import at.bitfire.dav4jvm.DavOCResource
import okhttp3.Response
import java.net.URL


class CopyMethod(
    val url: URL,
    private val destinationUrl: String,
    val forceOverride: Boolean = false
) : DavMethod(url) {
    @Throws(Exception::class)
    public override fun onDavExecute(davResource: DavOCResource): Int {
        davResource.copy(
            destinationUrl,
            forceOverride,
            super.getRequestHeadersAsHashMap()
        ) { callBackResponse: Response ->
            response = callBackResponse
        }
        return super.statusCode
    }
}
