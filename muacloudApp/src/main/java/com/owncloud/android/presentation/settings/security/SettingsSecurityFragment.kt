

package com.owncloud.android.presentation.settings.security

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.preference.CheckBoxPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import com.owncloud.android.R
import com.owncloud.android.extensions.avoidScreenshotsIfNeeded
import com.owncloud.android.extensions.showMessageInSnackbar
import com.owncloud.android.presentation.documentsprovider.DocumentsProviderUtils.Companion.notifyDocumentsProviderRoots
import com.owncloud.android.presentation.security.LockTimeout
import com.owncloud.android.presentation.security.PREFERENCE_LOCK_TIMEOUT
import com.owncloud.android.presentation.security.biometric.BiometricActivity
import com.owncloud.android.presentation.security.biometric.BiometricManager
import com.owncloud.android.presentation.security.passcode.PassCodeActivity
import com.owncloud.android.presentation.security.pattern.PatternActivity
import com.owncloud.android.presentation.settings.SettingsFragment.Companion.removePreferenceFromScreen
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsSecurityFragment : PreferenceFragmentCompat() {

    private val securityViewModel by viewModel<SettingsSecurityViewModel>()

    private var screenSecurity: PreferenceScreen? = null
    private var prefPasscode: CheckBoxPreference? = null
    private var prefPattern: CheckBoxPreference? = null
    private var prefBiometric: CheckBoxPreference? = null
    private var prefLockApplication: ListPreference? = null
    private var prefLockAccessDocumentProvider: CheckBoxPreference? = null
    private var prefTouchesWithOtherVisibleWindows: CheckBoxPreference? = null

    private val enablePasscodeLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult
            else {
                prefPasscode?.isChecked = true
                prefBiometric?.isChecked = securityViewModel.getBiometricsState()

                enableBiometricAndLockApplication()
            }
        }

    private val disablePasscodeLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult
            else {
                prefPasscode?.isChecked = false

                disableBiometric()
                prefLockApplication?.isEnabled = false
            }
        }

    private val enablePatternLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult
            else {
                prefPattern?.isChecked = true
                prefBiometric?.isChecked = securityViewModel.getBiometricsState()

                enableBiometricAndLockApplication()
            }
        }

    private val disablePatternLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult
            else {
                prefPattern?.isChecked = false

                disableBiometric()
                prefLockApplication?.isEnabled = false
            }
        }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_security, rootKey)

        screenSecurity = findPreference(SCREEN_SECURITY)
        prefPasscode = findPreference(PassCodeActivity.PREFERENCE_SET_PASSCODE)
        prefPattern = findPreference(PatternActivity.PREFERENCE_SET_PATTERN)
        prefBiometric = findPreference(BiometricActivity.PREFERENCE_SET_BIOMETRIC)
        prefLockApplication = findPreference<ListPreference>(PREFERENCE_LOCK_TIMEOUT)?.apply {
            entries = listOf(
                getString(R.string.prefs_lock_application_entries_immediately),
                getString(R.string.prefs_lock_application_entries_1minute),
                getString(R.string.prefs_lock_application_entries_5minutes),
                getString(R.string.prefs_lock_application_entries_30minutes)
            ).toTypedArray()
            entryValues = listOf(
                LockTimeout.IMMEDIATELY.name,
                LockTimeout.ONE_MINUTE.name,
                LockTimeout.FIVE_MINUTES.name,
                LockTimeout.THIRTY_MINUTES.name
            ).toTypedArray()
            isEnabled = !securityViewModel.isLockDelayEnforcedEnabled()
        }
        prefLockAccessDocumentProvider = findPreference(PREFERENCE_LOCK_ACCESS_FROM_DOCUMENT_PROVIDER)
        prefTouchesWithOtherVisibleWindows = findPreference(PREFERENCE_TOUCHES_WITH_OTHER_VISIBLE_WINDOWS)

        prefPasscode?.isVisible = !securityViewModel.isSecurityEnforcedEnabled()
        prefPattern?.isVisible = !securityViewModel.isSecurityEnforcedEnabled()

        prefPasscode?.setOnPreferenceChangeListener { _: Preference?, newValue: Any ->
            if (securityViewModel.isPatternSet()) {
                showMessageInSnackbar(getString(R.string.pattern_already_set))
            } else {
                val intent = Intent(activity, PassCodeActivity::class.java)
                if (newValue as Boolean) {
                    intent.action = PassCodeActivity.ACTION_CREATE
                    enablePasscodeLauncher.launch(intent)
                } else {
                    intent.action = PassCodeActivity.ACTION_REMOVE
                    disablePasscodeLauncher.launch(intent)
                }
            }
            false
        }

        prefPattern?.setOnPreferenceChangeListener { _: Preference?, newValue: Any ->
            if (securityViewModel.isPasscodeSet()) {
                showMessageInSnackbar(getString(R.string.passcode_already_set))
            } else {
                val intent = Intent(activity, PatternActivity::class.java)
                if (newValue as Boolean) {
                    intent.action = PatternActivity.ACTION_REQUEST_WITH_RESULT
                    enablePatternLauncher.launch(intent)
                } else {
                    intent.action = PatternActivity.ACTION_CHECK_WITH_RESULT
                    disablePatternLauncher.launch(intent)
                }
            }
            false
        }

        if (prefBiometric != null) {
            if (!BiometricManager.isHardwareDetected()) { // Biometric not supported
                screenSecurity?.removePreferenceFromScreen(prefBiometric)
            } else {
                if (prefPasscode?.isChecked == false && prefPattern?.isChecked == false) { // Disable biometric lock if Passcode or Pattern locks are disabled
                    disableBiometric()
                }

                prefBiometric?.setOnPreferenceChangeListener { _: Preference?, newValue: Any ->
                    val incomingValue = newValue as Boolean

                    if (incomingValue && !BiometricManager.hasEnrolledBiometric()) {
                        showMessageInSnackbar(getString(R.string.biometric_not_enrolled))
                        return@setOnPreferenceChangeListener false
                    }
                    true
                }
            }
        }

        if (prefPasscode?.isChecked == false && prefPattern?.isChecked == false) {
            prefLockApplication?.isEnabled = false
        }

        prefLockAccessDocumentProvider?.setOnPreferenceChangeListener { _: Preference?, newValue: Any ->
            securityViewModel.setPrefLockAccessDocumentProvider(true)
            notifyDocumentsProviderRoots(requireContext())
            true
        }

        prefTouchesWithOtherVisibleWindows?.setOnPreferenceChangeListener { _: Preference?, newValue: Any ->
            if (newValue as Boolean) {
                activity?.let {
                    AlertDialog.Builder(it)
                        .setTitle(getString(R.string.confirmation_touches_with_other_windows_title))
                        .setMessage(getString(R.string.confirmation_touches_with_other_windows_message))
                        .setNegativeButton(getString(R.string.common_no), null)
                        .setPositiveButton(
                            getString(R.string.common_yes)
                        ) { _: DialogInterface?, _: Int ->
                            securityViewModel.setPrefTouchesWithOtherVisibleWindows(true)
                            prefTouchesWithOtherVisibleWindows?.isChecked = true
                        }
                        .show()
                        .avoidScreenshotsIfNeeded()
                }
                return@setOnPreferenceChangeListener false
            }
            true
        }
    }

    private fun enableBiometricAndLockApplication() {
        prefBiometric?.apply {
            isEnabled = true
            summary = null
        }
        prefLockApplication?.isEnabled = !securityViewModel.isLockDelayEnforcedEnabled()
    }

    private fun disableBiometric() {
        prefBiometric?.apply {
            isChecked = false
            isEnabled = false
            summary = getString(R.string.prefs_biometric_summary)
        }
    }

    companion object {
        private const val SCREEN_SECURITY = "security_screen"
        const val PREFERENCE_LOCK_ACCESS_FROM_DOCUMENT_PROVIDER = "lock_access_from_document_provider"
        const val PREFERENCE_TOUCHES_WITH_OTHER_VISIBLE_WINDOWS = "touches_with_other_visible_windows"
        const val EXTRAS_LOCK_ENFORCED = "EXTRAS_LOCK_ENFORCED"
        const val PREFERENCE_LOCK_ATTEMPTS = "PrefLockAttempts"
    }
}
