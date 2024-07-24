

package com.owncloud.android.providers

import android.content.Context

interface ContextProvider {

    fun getBoolean(id: Int): Boolean
    fun getString(id: Int): String
    fun getInt(id: Int): Int
    fun getContext(): Context
    fun isConnected(): Boolean
}
