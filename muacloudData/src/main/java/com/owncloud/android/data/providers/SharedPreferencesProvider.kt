

package com.owncloud.android.data.providers

interface SharedPreferencesProvider {

    fun putString(key: String, value: String)
    fun getString(key: String, defaultValue: String?): String?

    fun putInt(key: String, value: Int)
    fun getInt(key: String, defaultValue: Int): Int

    fun putLong(key: String, value: Long)
    fun getLong(key: String, defaultValue: Long): Long

    fun putBoolean(key: String, value: Boolean)
    fun getBoolean(key: String, defaultValue: Boolean): Boolean

    fun containsPreference(key: String): Boolean

    fun removePreference(key: String)

    fun contains(key: String): Boolean
}
