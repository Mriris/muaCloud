

package com.owncloud.android.presentation.security.biometric

interface EnableBiometrics {
    fun onOptionSelected(optionSelected: BiometricStatus)
}

enum class BiometricStatus {
    ENABLED_BY_USER, DISABLED_BY_USER
}
