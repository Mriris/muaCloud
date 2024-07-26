
package com.owncloud.android.extensions

import android.content.Context
import com.owncloud.android.utils.DisplayUtils
import java.io.File

fun File.toLegibleStringSize(context: Context): String {
    val bytes = if (!exists()) 0L else length()
    return DisplayUtils.bytesToHumanReadable(bytes, context)
}
