
package com.owncloud.android.lib.common.utils

import timber.log.Timber
import java.io.File

object LoggingHelper {

    fun startLogging(directory: File, storagePath: String) {
        ocFileLoggingTree()?.let {
            Timber.uproot(it)
        }
        if (!directory.exists())
            directory.mkdirs()
        Timber.plant(OCFileLoggingTree(directory, filename = storagePath))
    }

    fun stopLogging() {
        ocFileLoggingTree()?.let {
            Timber.uproot(it)
        }
    }
}
