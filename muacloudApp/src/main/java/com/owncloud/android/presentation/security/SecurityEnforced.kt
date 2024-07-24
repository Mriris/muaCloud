

package com.owncloud.android.presentation.security

interface SecurityEnforced {
    fun optionLockSelected(type: LockType)
}

enum class LockType {
    PASSCODE, PATTERN;

    companion object {
        fun parseFromInteger(value: Int): LockType {
            return when (value) {
                0 -> PASSCODE
                else -> PATTERN
            }
        }
    }
}
