package com.owncloud.android.lib.common.http.methods.webdav

import at.bitfire.dav4jvm.DavOCResource
import okhttp3.Response
import java.net.URL


class MkColMethod(url: URL) : DavMethod(url) {
    @Throws(Exception::class)
    public override fun onDavExecute(davResource: DavOCResource): Int {
        davResource.mkCol(
            xmlBody = null,
            listOfHeaders = super.getRequestHeadersAsHashMap()
        ) { callBackResponse: Response ->
            response = callBackResponse
        }
        return super.statusCode
    }
}
