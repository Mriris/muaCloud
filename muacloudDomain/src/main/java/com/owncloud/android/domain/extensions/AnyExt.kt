

package com.owncloud.android.domain.extensions

fun Any.isOneOf(vararg values: Any): Boolean {
    return this in values
}
