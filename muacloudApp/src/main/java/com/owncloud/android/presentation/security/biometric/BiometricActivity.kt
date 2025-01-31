

package com.owncloud.android.presentation.security.biometric

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import com.owncloud.android.R
import com.owncloud.android.presentation.security.passcode.PassCodeActivity
import com.owncloud.android.presentation.security.passcode.PassCodeManager
import com.owncloud.android.presentation.security.pattern.PatternManager
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.util.concurrent.Executor
import com.owncloud.android.presentation.security.biometric.BiometricManager as OwnCloudBiometricManager

class BiometricActivity : AppCompatActivity() {

    private val biometricViewModel by viewModel<BiometricViewModel>()

    private lateinit var cryptoObject: BiometricPrompt.CryptoObject
    private val handler = Handler()
    private val executor = Executor { command -> handler.post(command) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        biometricViewModel.initCipher()?.let {
            cryptoObject = BiometricPrompt.CryptoObject(it)
        }

        val biometricManager = BiometricManager.from(this)
        if (biometricManager.canAuthenticate(BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS) {
            showBiometricPrompt()
        } else {
            authError()
        }
    }

    private fun showBiometricPrompt() {
        val promptInfo = PromptInfo.Builder()
            .setTitle(getString(R.string.biometric_prompt_title))
            .setSubtitle(getString(R.string.biometric_prompt_subtitle))
            .setNegativeButtonText(getString(android.R.string.cancel))
            .setConfirmationRequired(true)
            .build()
        val biometricPrompt = BiometricPrompt(this@BiometricActivity,
            executor, object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Timber.e("onAuthenticationError ($errorCode): $errString")
                    authError()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    if (result.cryptoObject?.cipher != cryptoObject.cipher) {
                        authError()
                    } else {
                        if (biometricViewModel.shouldAskForNewPassCode()) {
                            biometricViewModel.removePassCode()
                            val intent = Intent(baseContext, PassCodeActivity::class.java)
                            intent.action = PassCodeActivity.ACTION_CREATE
                            intent.putExtra(PassCodeActivity.EXTRAS_MIGRATION, true)
                            startActivity(intent)
                        }
                        biometricViewModel.setLastUnlockTimestamp()
                        OwnCloudBiometricManager.onActivityStopped(this@BiometricActivity)
                        finish()
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Timber.e("onAuthenticationFailed")
                }
            })

        biometricPrompt.authenticate(promptInfo, cryptoObject)
    }

    private fun authError() {
        if (PassCodeManager.isPassCodeEnabled()) {
            PassCodeManager.onBiometricCancelled(this)
        } else if (PatternManager.isPatternEnabled()) {
            PatternManager.onBiometricCancelled(this)
        }

        finish()
    }

    companion object {
        const val PREFERENCE_SET_BIOMETRIC = "set_biometric"

        const val KEY_NAME = "default_key"
    }
}
