

package com.owncloud.android.presentation.security

enum class LockEnforcedType {
    DISABLED, EITHER_ENFORCED, PASSCODE_ENFORCED, PATTERN_ENFORCED;

    companion object {
        fun parseFromInteger(int: Int): LockEnforcedType =
            when (int) {
                1 -> EITHER_ENFORCED
                2 -> PASSCODE_ENFORCED
                3 -> PATTERN_ENFORCED
                else -> DISABLED
            }
    }
}
