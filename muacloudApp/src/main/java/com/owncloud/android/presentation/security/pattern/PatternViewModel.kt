

package com.owncloud.android.presentation.security.pattern

import androidx.lifecycle.ViewModel
import com.owncloud.android.data.providers.SharedPreferencesProvider
import com.owncloud.android.presentation.security.biometric.BiometricActivity

class PatternViewModel(
    private val preferencesProvider: SharedPreferencesProvider
) : ViewModel() {

    fun setPattern(pattern: String) {
        preferencesProvider.putString(PatternActivity.PREFERENCE_PATTERN, pattern)
        preferencesProvider.putBoolean(PatternActivity.PREFERENCE_SET_PATTERN, true)
    }

    fun removePattern() {
        preferencesProvider.removePreference(PatternActivity.PREFERENCE_PATTERN)
        preferencesProvider.putBoolean(PatternActivity.PREFERENCE_SET_PATTERN, false)
    }

    fun checkPatternIsValid(patternValue: String?): Boolean {
        val savedPattern = preferencesProvider.getString(PatternActivity.PREFERENCE_PATTERN, null)
        return savedPattern != null && savedPattern == patternValue
    }

    fun setBiometricsState(enabled: Boolean) {
        preferencesProvider.putBoolean(BiometricActivity.PREFERENCE_SET_BIOMETRIC, enabled)
    }
}
