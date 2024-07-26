package com.owncloud.android.lib.common.http.methods.nonwebdav

import okhttp3.OkHttpClient
import java.io.IOException
import java.net.URL


class DeleteMethod(url: URL) : HttpMethod(url) {
    @Throws(IOException::class)
    override fun onExecute(okHttpClient: OkHttpClient): Int {
        request = request.newBuilder()
            .delete()
            .build()
        return super.onExecute(okHttpClient)
    }
}
