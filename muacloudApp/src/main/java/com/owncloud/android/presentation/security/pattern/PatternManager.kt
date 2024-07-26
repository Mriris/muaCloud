

package com.owncloud.android.presentation.security.pattern

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.os.SystemClock
import com.owncloud.android.MainApp.Companion.appContext
import com.owncloud.android.data.providers.implementation.OCSharedPreferencesProvider
import com.owncloud.android.presentation.security.LockTimeout
import com.owncloud.android.presentation.security.PREFERENCE_LAST_UNLOCK_TIMESTAMP
import com.owncloud.android.presentation.security.PREFERENCE_LOCK_TIMEOUT
import com.owncloud.android.presentation.security.bayPassUnlockOnce
import com.owncloud.android.presentation.security.biometric.BiometricManager
import kotlin.math.abs

object PatternManager {

    private val exemptOfPatternActivities: MutableSet<Class<*>> = mutableSetOf(PatternActivity::class.java)
    private val visibleActivities: MutableSet<Class<*>> = mutableSetOf()
    private val preferencesProvider = OCSharedPreferencesProvider(appContext)

    fun onActivityStarted(activity: Activity) {
        if (!exemptOfPatternActivities.contains(activity.javaClass) && patternShouldBeRequested()) {

            if (BiometricManager.isBiometricEnabled() && !visibleActivities.contains(
                    PatternActivity::class.java
                )
            ) {
                visibleActivities.add(activity.javaClass)
                return
            }

            askUserForPattern(activity)
        }

        visibleActivities.add(activity.javaClass)
    }

    fun onActivityStopped(activity: Activity) {
        visibleActivities.remove(activity.javaClass)

        bayPassUnlockOnce()
        val powerMgr = activity.getSystemService(Context.POWER_SERVICE) as PowerManager
        if (isPatternEnabled() && !powerMgr.isInteractive) {
            activity.moveTaskToBack(true)
        }
    }

    private fun patternShouldBeRequested(): Boolean {
        val lastUnlockTimestamp = preferencesProvider.getLong(PREFERENCE_LAST_UNLOCK_TIMESTAMP, 0)
        val timeout = LockTimeout.valueOf(preferencesProvider.getString(PREFERENCE_LOCK_TIMEOUT, LockTimeout.IMMEDIATELY.name)!!).toMilliseconds()
        return if (visibleActivities.contains(PatternActivity::class.java)) isPatternEnabled()
        else if (abs(SystemClock.elapsedRealtime() - lastUnlockTimestamp) > timeout && visibleActivities.isEmpty()) isPatternEnabled()
        else false
    }

    fun isPatternEnabled(): Boolean {
        return preferencesProvider.getBoolean(PatternActivity.PREFERENCE_SET_PATTERN, false)
    }

    private fun askUserForPattern(activity: Activity) {
        val i = Intent(appContext, PatternActivity::class.java).apply {
            action = PatternActivity.ACTION_CHECK
            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        activity.startActivity(i)
    }

    fun onBiometricCancelled(activity: Activity) {
        askUserForPattern(activity)
    }
}
