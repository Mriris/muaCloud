

package com.owncloud.android.presentation.security.passcode

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

object PassCodeManager {

    private val exemptOfPasscodeActivities: MutableSet<Class<*>> = mutableSetOf(PassCodeActivity::class.java)
    private val visibleActivities: MutableSet<Class<*>> = mutableSetOf()
    private val preferencesProvider = OCSharedPreferencesProvider(appContext)

    fun onActivityStarted(activity: Activity) {
        if (!exemptOfPasscodeActivities.contains(activity.javaClass) && passCodeShouldBeRequested()) {

            if (BiometricManager.isBiometricEnabled() && !visibleActivities.contains(
                    PassCodeActivity::class.java
                )
            ) {
                visibleActivities.add(activity.javaClass)
                return
            }

            askUserForPasscode(activity)
        } else if (preferencesProvider.getBoolean(PassCodeActivity.PREFERENCE_MIGRATION_REQUIRED, false)) {
            val intent = Intent(appContext, PassCodeActivity::class.java).apply {
                action = PassCodeActivity.ACTION_CREATE
                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(PassCodeActivity.EXTRAS_MIGRATION, true)
            }
            activity.startActivity(intent)
        }

        visibleActivities.add(activity.javaClass) // keep it AFTER passCodeShouldBeRequested was checked
    }

    fun onActivityStopped(activity: Activity) {
        visibleActivities.remove(activity.javaClass)

        bayPassUnlockOnce()
        val powerMgr = activity.getSystemService(Context.POWER_SERVICE) as PowerManager
        if (isPassCodeEnabled() && !powerMgr.isInteractive) {
            activity.moveTaskToBack(true)
        }
    }

    private fun passCodeShouldBeRequested(): Boolean {
        val lastUnlockTimestamp = preferencesProvider.getLong(PREFERENCE_LAST_UNLOCK_TIMESTAMP, 0)
        val timeout = LockTimeout.valueOf(preferencesProvider.getString(PREFERENCE_LOCK_TIMEOUT, LockTimeout.IMMEDIATELY.name)!!).toMilliseconds()
        return if (visibleActivities.contains(PassCodeActivity::class.java)) isPassCodeEnabled()
        else if (abs(SystemClock.elapsedRealtime() - lastUnlockTimestamp) > timeout && visibleActivities.isEmpty()) isPassCodeEnabled()
        else false
    }

    fun isPassCodeEnabled(): Boolean {
        return preferencesProvider.getBoolean(PassCodeActivity.PREFERENCE_SET_PASSCODE, false)
    }

    private fun askUserForPasscode(activity: Activity) {
        val i = Intent(appContext, PassCodeActivity::class.java).apply {
            action = PassCodeActivity.ACTION_CHECK
            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        activity.startActivity(i)
    }

    fun onBiometricCancelled(activity: Activity) {
        askUserForPasscode(activity)
    }
}
