
package com.owncloud.android.lib.common.http

import com.facebook.stetho.okhttp3.StethoInterceptor

object DebugInterceptorFactory {
    fun getInterceptor() = StethoInterceptor()
}
