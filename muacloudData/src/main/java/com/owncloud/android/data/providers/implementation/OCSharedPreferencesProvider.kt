

package com.owncloud.android.data.providers.implementation

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.owncloud.android.data.providers.SharedPreferencesProvider

class OCSharedPreferencesProvider(
    context: Context
) : SharedPreferencesProvider {

    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val editor = sharedPreferences.edit()

    override fun putString(key: String, value: String) = editor.putString(key, value).apply()
    override fun getString(key: String, defaultValue: String?) = sharedPreferences.getString(key, defaultValue)

    override fun putInt(key: String, value: Int) = editor.putInt(key, value).apply()
    override fun getInt(key: String, defaultValue: Int) = sharedPreferences.getInt(key, defaultValue)

    override fun putLong(key: String, value: Long) = editor.putLong(key, value).apply()
    override fun getLong(key: String, defaultValue: Long) = sharedPreferences.getLong(key, defaultValue)

    override fun putBoolean(key: String, value: Boolean) = editor.putBoolean(key, value).apply()
    override fun getBoolean(key: String, defaultValue: Boolean) = sharedPreferences.getBoolean(key, defaultValue)

    override fun containsPreference(key: String) = sharedPreferences.contains(key)

    override fun removePreference(key: String) = editor.remove(key).apply()

    override fun contains(key: String) = sharedPreferences.contains(key)
}
