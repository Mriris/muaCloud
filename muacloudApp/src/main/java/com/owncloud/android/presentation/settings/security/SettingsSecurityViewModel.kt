

package com.owncloud.android.presentation.settings.security

import androidx.lifecycle.ViewModel
import com.owncloud.android.R
import com.owncloud.android.data.providers.SharedPreferencesProvider
import com.owncloud.android.presentation.security.LockEnforcedType
import com.owncloud.android.presentation.security.LockEnforcedType.Companion.parseFromInteger
import com.owncloud.android.presentation.security.LockTimeout
import com.owncloud.android.presentation.security.biometric.BiometricActivity
import com.owncloud.android.presentation.security.isDeviceSecure
import com.owncloud.android.presentation.security.passcode.PassCodeActivity
import com.owncloud.android.presentation.security.pattern.PatternActivity
import com.owncloud.android.providers.MdmProvider
import com.owncloud.android.utils.CONFIGURATION_DEVICE_PROTECTION
import com.owncloud.android.utils.CONFIGURATION_LOCK_DELAY_TIME
import com.owncloud.android.utils.NO_MDM_RESTRICTION_YET

class SettingsSecurityViewModel(
    private val preferencesProvider: SharedPreferencesProvider,
    private val mdmProvider: MdmProvider,
) : ViewModel() {

    fun isPatternSet() = preferencesProvider.getBoolean(PatternActivity.PREFERENCE_SET_PATTERN, false)

    fun isPasscodeSet() = preferencesProvider.getBoolean(PassCodeActivity.PREFERENCE_SET_PASSCODE, false)

    fun setPrefLockAccessDocumentProvider(value: Boolean) =
        preferencesProvider.putBoolean(SettingsSecurityFragment.PREFERENCE_LOCK_ACCESS_FROM_DOCUMENT_PROVIDER, value)

    fun setPrefTouchesWithOtherVisibleWindows(value: Boolean) =
        preferencesProvider.putBoolean(SettingsSecurityFragment.PREFERENCE_TOUCHES_WITH_OTHER_VISIBLE_WINDOWS, value)

    fun getBiometricsState(): Boolean = preferencesProvider.getBoolean(BiometricActivity.PREFERENCE_SET_BIOMETRIC, false)

    fun isSecurityEnforcedEnabled() =
        (mdmProvider.getBrandingBoolean(CONFIGURATION_DEVICE_PROTECTION, R.bool.device_protection) && !isDeviceSecure()) ||
                parseFromInteger(mdmProvider.getBrandingInteger(NO_MDM_RESTRICTION_YET, R.integer.lock_enforced)) != LockEnforcedType.DISABLED

    fun isLockDelayEnforcedEnabled() = LockTimeout.parseFromInteger(
        mdmProvider.getBrandingInteger(
            mdmKey = CONFIGURATION_LOCK_DELAY_TIME,
            integerKey = R.integer.lock_delay_enforced
        )
    ) != LockTimeout.DISABLED
}
