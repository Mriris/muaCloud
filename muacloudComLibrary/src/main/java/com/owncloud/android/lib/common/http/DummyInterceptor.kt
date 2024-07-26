
package com.owncloud.android.lib.common.http

import okhttp3.Interceptor

class DummyInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain) = chain.proceed(chain.request())
}