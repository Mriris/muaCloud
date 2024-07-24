

package com.owncloud.android.utils

import android.content.Context
import android.os.Build
import android.os.StrictMode
import com.facebook.stetho.Stetho

object DebugInjector {
    open fun injectDebugTools(context: Context) {
        Stetho.initializeWithDefaults(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .penaltyLog()
                    .detectNonSdkApiUsage()
                    .detectUnsafeIntentLaunch()
                    .build()
            )
        }
    }
}
