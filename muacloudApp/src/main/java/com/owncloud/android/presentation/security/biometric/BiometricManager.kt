

package com.owncloud.android.presentation.security.biometric

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.os.SystemClock
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import com.owncloud.android.MainApp.Companion.appContext
import com.owncloud.android.data.providers.implementation.OCSharedPreferencesProvider
import com.owncloud.android.presentation.security.LockTimeout
import com.owncloud.android.presentation.security.PREFERENCE_LAST_UNLOCK_TIMESTAMP
import com.owncloud.android.presentation.security.PREFERENCE_LOCK_TIMEOUT
import com.owncloud.android.presentation.security.bayPassUnlockOnce
import com.owncloud.android.presentation.security.passcode.PassCodeActivity
import com.owncloud.android.presentation.security.passcode.PassCodeManager
import com.owncloud.android.presentation.security.passcode.PassCodeManager.isPassCodeEnabled
import com.owncloud.android.presentation.security.pattern.PatternActivity
import com.owncloud.android.presentation.security.pattern.PatternManager
import com.owncloud.android.presentation.security.pattern.PatternManager.isPatternEnabled
import kotlin.math.abs

object BiometricManager {

    private val exemptOfBiometricActivities: MutableSet<Class<*>> =
        mutableSetOf(BiometricActivity::class.java, PassCodeActivity::class.java, PatternActivity::class.java)
    private val visibleActivities: MutableSet<Class<*>> = mutableSetOf()
    private val preferencesProvider = OCSharedPreferencesProvider(appContext)
    private val biometricManager: BiometricManager = BiometricManager.from(appContext)

    fun onActivityStarted(activity: Activity) {
        if (!exemptOfBiometricActivities.contains(activity.javaClass) && biometricShouldBeRequested()) {

            if (isHardwareDetected() && hasEnrolledBiometric()) {

                val i = Intent(appContext, BiometricActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                activity.startActivity(i)
            } else if (isPassCodeEnabled()) {

                PassCodeManager.onBiometricCancelled(activity)
                visibleActivities.add(activity.javaClass)
            } else if (isPatternEnabled()) {

                PatternManager.onBiometricCancelled(activity)
                visibleActivities.add(activity.javaClass)
            }

        }

        visibleActivities.add(activity.javaClass) // keep it AFTER biometricShouldBeRequested was checked

    }

    fun onActivityStopped(activity: Activity) {
        visibleActivities.remove(activity.javaClass)

        bayPassUnlockOnce()
        val powerMgr = activity.getSystemService(Context.POWER_SERVICE) as PowerManager
        if (isBiometricEnabled() && !powerMgr.isInteractive) {
            activity.moveTaskToBack(true)
        }
    }

    private fun biometricShouldBeRequested(): Boolean {
        val lastUnlockTimestamp = preferencesProvider.getLong(PREFERENCE_LAST_UNLOCK_TIMESTAMP, 0)
        val timeout = LockTimeout.valueOf(preferencesProvider.getString(PREFERENCE_LOCK_TIMEOUT, LockTimeout.IMMEDIATELY.name)!!).toMilliseconds()
        return if (visibleActivities.contains(BiometricActivity::class.java)) isBiometricEnabled()
        else if (abs(SystemClock.elapsedRealtime() - lastUnlockTimestamp) > timeout && visibleActivities.isEmpty()) isBiometricEnabled()
        else false
    }

    fun isBiometricEnabled(): Boolean {
        return preferencesProvider.getBoolean(BiometricActivity.PREFERENCE_SET_BIOMETRIC, false)
    }

    fun isHardwareDetected(): Boolean {
        return biometricManager.canAuthenticate(BIOMETRIC_WEAK) != BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE &&
                biometricManager.canAuthenticate(BIOMETRIC_WEAK) != BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE
    }

    fun hasEnrolledBiometric(): Boolean {
        return biometricManager.canAuthenticate(BIOMETRIC_WEAK) != BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED
    }
}
