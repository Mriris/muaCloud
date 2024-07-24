

package com.owncloud.android.providers.implementation

import android.content.Context
import com.owncloud.android.providers.ContextProvider
import com.owncloud.android.utils.ConnectivityUtils

class OCContextProvider(private val context: Context) : ContextProvider {

    override fun getBoolean(id: Int): Boolean = context.resources.getBoolean(id)

    override fun getString(id: Int): String = context.resources.getString(id)

    override fun getInt(id: Int): Int = context.resources.getInteger(id)

    override fun getContext(): Context = context

    override fun isConnected(): Boolean {
        return ConnectivityUtils.isAppConnected(context)
    }
}
