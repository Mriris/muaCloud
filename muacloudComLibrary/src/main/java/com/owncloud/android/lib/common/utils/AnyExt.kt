
package com.owncloud.android.lib.common.utils

fun Any.isOneOf(vararg values: Any): Boolean {
    return this in values
}
