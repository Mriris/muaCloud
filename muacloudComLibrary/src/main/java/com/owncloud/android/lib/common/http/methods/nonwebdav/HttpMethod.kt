package com.owncloud.android.lib.common.http.methods.nonwebdav

import com.owncloud.android.lib.common.http.methods.HttpBaseMethod
import okhttp3.OkHttpClient
import okhttp3.Response
import java.net.URL


abstract class HttpMethod(
    url: URL
) : HttpBaseMethod(url) {

    override lateinit var response: Response

    public override fun onExecute(okHttpClient: OkHttpClient): Int {
        call = okHttpClient.newCall(request)
        call?.let { response = it.execute() }
        return super.statusCode
    }
}
