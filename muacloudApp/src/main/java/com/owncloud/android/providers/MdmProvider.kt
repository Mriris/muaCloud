

package com.owncloud.android.providers

import android.content.Context
import android.content.RestrictionsManager
import androidx.annotation.BoolRes
import androidx.annotation.IntegerRes
import androidx.annotation.StringRes
import androidx.enterprise.feedback.KeyedAppState
import androidx.enterprise.feedback.KeyedAppStatesReporter
import com.owncloud.android.BuildConfig
import com.owncloud.android.MainApp.Companion.MDM_FLAVOR
import com.owncloud.android.data.providers.implementation.OCSharedPreferencesProvider
import com.owncloud.android.utils.MDMConfigurations
import timber.log.Timber

class MdmProvider(
    private val context: Context
) {
    private val restrictionsManager = context.getSystemService(Context.RESTRICTIONS_SERVICE) as RestrictionsManager
    private val restrictions = restrictionsManager.applicationRestrictions
    private val preferencesProvider = OCSharedPreferencesProvider(context)
    private val restrictionsReporter = KeyedAppStatesReporter.create(context)

    fun cacheStringRestriction(key: String, idMessageFeedback: Int) {
        if (!restrictions.containsKey(key)) {

            preferencesProvider.removePreference(key = key)
            return
        }

        val newRestriction = restrictions.getString(key)
        val isRestrictionAlreadyCached = preferencesProvider.contains(key)
        val oldRestriction = preferencesProvider.getString(key, null)

        newRestriction?.let { preferencesProvider.putString(key, it) }
        if (!isRestrictionAlreadyCached || !newRestriction.equals(oldRestriction)) {
            sendFeedback(
                key = key,
                message = context.getString(idMessageFeedback)
            )
        }
        Timber.i("MDM | key: $key | New value: $newRestriction | Old value: $oldRestriction")
    }

    fun cacheBooleanRestriction(key: String, idMessageFeedback: Int) {
        if (!restrictions.containsKey(key)) {

            preferencesProvider.removePreference(key = key)
            return
        }

        val newRestriction = restrictions.getBoolean(key)
        val isRestrictionAlreadyCached = preferencesProvider.contains(key)
        val oldRestriction = preferencesProvider.getBoolean(key, false)

        preferencesProvider.putBoolean(key, newRestriction)
        if (!isRestrictionAlreadyCached || (newRestriction != oldRestriction)) {
            sendFeedback(
                key = key,
                message = context.getString(idMessageFeedback)
            )
        }
        Timber.i("MDM | key: $key | New value: $newRestriction | Old value: $oldRestriction")
    }

    fun cacheIntegerRestriction(key: String, idMessageFeedback: Int) {
        if (!restrictions.containsKey(key)) {

            preferencesProvider.removePreference(key = key)
            return
        }

        val newRestriction = restrictions.getInt(key)
        val isRestrictionAlreadyCached = preferencesProvider.contains(key)
        val oldRestriction = preferencesProvider.getInt(key, 0)

        preferencesProvider.putInt(key, newRestriction)
        if (!isRestrictionAlreadyCached || (newRestriction != oldRestriction)) {
            sendFeedback(
                key = key,
                message = context.getString(idMessageFeedback)
            )
        }
        Timber.i("MDM | key: $key | New value: $newRestriction | Old value: $oldRestriction")
    }

    fun getBrandingString(
        @MDMConfigurations mdmKey: String,
        @StringRes stringKey: Int,
    ): String {
        val setupValue: String = context.resources.getString(stringKey)

        return if (isMdmFlavor()) {
            preferencesProvider.getString(key = mdmKey, defaultValue = setupValue) ?: throw IllegalStateException("Key $stringKey is not supported")
        } else setupValue
    }

    fun getBrandingBoolean(
        @MDMConfigurations mdmKey: String,
        @BoolRes booleanKey: Int
    ): Boolean {
        val setupValue = context.resources.getBoolean(booleanKey)

        return if (isMdmFlavor()) preferencesProvider.getBoolean(key = mdmKey, defaultValue = setupValue) else setupValue
    }

    fun getBrandingInteger(
        @MDMConfigurations mdmKey: String,
        @IntegerRes integerKey: Int
    ): Int {
        val setupValue = context.resources.getInteger(integerKey)

        return if (isMdmFlavor()) preferencesProvider.getInt(key = mdmKey, defaultValue = setupValue) else setupValue
    }

    private fun sendFeedback(
        reporter: KeyedAppStatesReporter = restrictionsReporter,
        key: String,
        isError: Boolean = false,
        message: String,
        data: String? = null
    ) {
        val severity = if (isError) KeyedAppState.SEVERITY_ERROR else KeyedAppState.SEVERITY_INFO
        val states = hashSetOf(
            KeyedAppState.builder()
                .setKey(key)
                .setSeverity(severity)
                .setMessage(message)
                .setData(data)
                .build()
        )
        reporter.setStates(states, null)
    }

    private fun isMdmFlavor() = BuildConfig.FLAVOR == MDM_FLAVOR
}
