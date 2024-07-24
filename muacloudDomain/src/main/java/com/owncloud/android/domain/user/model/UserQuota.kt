

package com.owncloud.android.domain.user.model

import androidx.annotation.VisibleForTesting
import kotlin.math.roundToLong

data class UserQuota(
    val accountName: String,
    val available: Long,
    val used: Long
) {
    @VisibleForTesting
    fun isLimited() = available > 0

    fun getRelative() = if (isLimited() && getTotal() > 0) {
        val relativeQuota = (used * 100).toDouble() / getTotal()
        (relativeQuota * 100).roundToLong() / 100.0
    } else 0.0

    fun getTotal() = if (isLimited()) {
        available + used
    } else {
        0
    }
}
