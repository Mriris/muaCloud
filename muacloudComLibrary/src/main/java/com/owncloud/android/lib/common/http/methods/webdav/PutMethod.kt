package com.owncloud.android.lib.common.http.methods.webdav

import at.bitfire.dav4jvm.DavOCResource
import at.bitfire.dav4jvm.exception.HttpException
import com.owncloud.android.lib.common.http.HttpConstants
import okhttp3.RequestBody
import java.io.IOException
import java.net.URL


class PutMethod(
    url: URL,
    private val putRequestBody: RequestBody
) : DavMethod(url) {
    @Throws(IOException::class, HttpException::class)
    public override fun onDavExecute(davResource: DavOCResource): Int {
        davResource.put(
            putRequestBody,
            super.getRequestHeader(HttpConstants.IF_MATCH_HEADER),
            getRequestHeadersAsHashMap()
        ) { callBackResponse ->
            response = callBackResponse
        }
        return super.statusCode
    }
}
