

package com.owncloud.android.extensions

import android.app.Dialog
import android.view.WindowManager
import com.owncloud.android.BuildConfig
import com.owncloud.android.R

fun Dialog.avoidScreenshotsIfNeeded() {
    if (!BuildConfig.DEBUG && context.resources?.getBoolean(R.bool.allow_screenshots) == false) {
        window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }
}