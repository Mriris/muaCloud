package com.owncloud.android.lib.common.http.methods.webdav

import at.bitfire.dav4jvm.DavOCResource
import at.bitfire.dav4jvm.Property
import at.bitfire.dav4jvm.Response
import at.bitfire.dav4jvm.Response.HrefRelation
import at.bitfire.dav4jvm.exception.DavException
import java.io.IOException
import java.net.URL


class PropfindMethod(
    url: URL,
    private val depth: Int,
    private val propertiesToRequest: Array<Property.Name>
) : DavMethod(url) {

    val members: MutableList<Response>
    var root: Response?
        private set

    @Throws(IOException::class, DavException::class)
    public override fun onDavExecute(davResource: DavOCResource): Int {
        davResource.propfind(
            depth = depth,
            reqProp = propertiesToRequest,
            listOfHeaders = super.getRequestHeadersAsHashMap(),
            callback = { response: Response, hrefRelation: HrefRelation ->
                when (hrefRelation) {
                    HrefRelation.MEMBER -> members.add(response)
                    HrefRelation.SELF -> this.root = response
                    HrefRelation.OTHER -> {
                    }
                }
            }, rawCallback = { callBackResponse: okhttp3.Response ->
                response = callBackResponse
            })
        return statusCode
    }

    init {
        members = arrayListOf()
        this.root = null
    }
}
