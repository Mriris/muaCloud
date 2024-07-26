package com.owncloud.android.lib.common.http.methods.nonwebdav

import okhttp3.OkHttpClient
import okhttp3.RequestBody
import java.io.IOException
import java.net.URL


class PostMethod(
    url: URL,
    private val postRequestBody: RequestBody
) : HttpMethod(url) {
    @Throws(IOException::class)
    override fun onExecute(okHttpClient: OkHttpClient): Int {
        request = request.newBuilder()
            .post(postRequestBody)
            .build()
        return super.onExecute(okHttpClient)
    }
}
