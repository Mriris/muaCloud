package com.owncloud.android.lib.common.http.methods.nonwebdav

import okhttp3.OkHttpClient
import java.io.IOException
import java.net.URL


class GetMethod(url: URL) : HttpMethod(url) {
    @Throws(IOException::class)
    override fun onExecute(okHttpClient: OkHttpClient): Int {
        request = request.newBuilder()
            .get()
            .build()
        return super.onExecute(okHttpClient)
    }
}
