
package com.owncloud.android.lib.common.http

object DebugInterceptorFactory {
    fun getInterceptor() = DummyInterceptor()
}