

package com.owncloud.android.presentation.security.passcode

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.WindowManager
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.owncloud.android.BuildConfig
import com.owncloud.android.R
import com.owncloud.android.databinding.PasscodelockBinding
import com.owncloud.android.domain.utils.Event
import com.owncloud.android.extensions.hideSoftKeyboard
import com.owncloud.android.extensions.showBiometricDialog
import com.owncloud.android.presentation.documentsprovider.DocumentsProviderUtils.Companion.notifyDocumentsProviderRoots
import com.owncloud.android.presentation.security.biometric.BiometricStatus
import com.owncloud.android.presentation.security.biometric.BiometricViewModel
import com.owncloud.android.presentation.security.biometric.EnableBiometrics
import com.owncloud.android.presentation.settings.security.SettingsSecurityFragment.Companion.EXTRAS_LOCK_ENFORCED
import com.owncloud.android.utils.PreferenceUtils
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class PassCodeActivity : AppCompatActivity(), NumberKeyboardListener, EnableBiometrics {

    private val passCodeViewModel: PassCodeViewModel by viewModel {
        parametersOf(
            getPasscodeAction(intent.action)
        )
    }

    private val biometricViewModel by viewModel<BiometricViewModel>()

    private var _binding: PasscodelockBinding? = null
    val binding get() = _binding!!

    private lateinit var passCodeEditTexts: Array<EditText?>

    private var numberOfPasscodeDigits = 0
    private var confirmingPassCode = false
    private val resultIntent = Intent()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

        subscribeToViewModel()

        _binding = PasscodelockBinding.inflate(layoutInflater)

        if (!BuildConfig.DEBUG) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } // else, let it go, or taking screenshots & testing will not be possible

        setContentView(binding.root)

        numberOfPasscodeDigits = passCodeViewModel.getPassCode()?.length ?: passCodeViewModel.getNumberOfPassCodeDigits()
        passCodeEditTexts = arrayOfNulls(numberOfPasscodeDigits)

        binding.passcodeLockLayout.filterTouchesWhenObscured =
            PreferenceUtils.shouldDisallowTouchesWithOtherVisibleWindows(this)
        binding.explanation.filterTouchesWhenObscured =
            PreferenceUtils.shouldDisallowTouchesWithOtherVisibleWindows(this)

        inflatePasscodeTxtLine()

        binding.numberKeyboard.setListener(this)

        if (passCodeViewModel.getNumberOfAttempts() >= NUM_ATTEMPTS_WITHOUT_TIMER) {
            lockScreen()
        }

        when (intent.action) {
            ACTION_CHECK -> { //When you start the app with passcode

                binding.header.text = getString(R.string.pass_code_enter_pass_code)
                binding.explanation.visibility = View.INVISIBLE
                supportActionBar?.setDisplayHomeAsUpEnabled(false) //Don´t show the back arrow
            }
            ACTION_CREATE -> { //Create a new password
                if (confirmingPassCode) {

                    requestPassCodeConfirmation()
                } else {
                    if (intent.extras?.getBoolean(EXTRAS_MIGRATION) == true) {
                        binding.header.text =
                            getString(R.string.pass_code_configure_your_pass_code_migration, passCodeViewModel.getNumberOfPassCodeDigits())
                    } else {


                        binding.header.text = getString(R.string.pass_code_configure_your_pass_code)
                    }
                    binding.explanation.visibility = View.VISIBLE
                    when {
                        intent.extras?.getBoolean(EXTRAS_MIGRATION) == true -> {
                            supportActionBar?.setDisplayHomeAsUpEnabled(false)
                        }
                        intent.extras?.getBoolean(EXTRAS_LOCK_ENFORCED) == true -> {
                            supportActionBar?.setDisplayHomeAsUpEnabled(false)
                        }
                        else -> {
                            supportActionBar?.setDisplayHomeAsUpEnabled(true)
                        }
                    }
                }
            }
            ACTION_REMOVE -> { // Remove password


                binding.header.text = getString(R.string.pass_code_remove_your_pass_code)
                binding.explanation.visibility = View.INVISIBLE
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
            }
            else -> {
                throw IllegalArgumentException(R.string.illegal_argument_exception_message.toString() + " ")
            }
        }

        setTextListeners()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        PassCodeManager.onActivityStopped(this)
        super.onBackPressed()
    }

    private fun inflatePasscodeTxtLine() {
        val layout_code = findViewById<LinearLayout>(R.id.layout_code)
        val numberOfPasscodeDigits = (passCodeViewModel.getPassCode()?.length ?: passCodeViewModel.getNumberOfPassCodeDigits())
        for (i in 0 until numberOfPasscodeDigits) {
            val txt = layoutInflater.inflate(R.layout.passcode_edit_text, layout_code, false) as EditText
            layout_code.addView(txt)
            passCodeEditTexts[i] = txt
        }
        passCodeEditTexts.first()?.requestFocus()
    }


    private fun setTextListeners() {
        val numberOfPasscodeDigits = (passCodeViewModel.getPassCode()?.length ?: passCodeViewModel.getNumberOfPassCodeDigits())
        for (i in 0 until numberOfPasscodeDigits) {
            passCodeEditTexts[i]?.setOnClickListener { hideSoftKeyboard() }
            passCodeEditTexts[i]?.onFocusChangeListener = OnFocusChangeListener { _: View, _: Boolean ->

                for (j in 0 until i) {
                    if (passCodeEditTexts[j]?.text.toString() == "") {  // TODO WIP validation


                        passCodeEditTexts[j]?.requestFocus()
                        break
                    }
                }
            }
        }
    }

    override fun onNumberClicked(number: Int) {
        passCodeViewModel.onNumberClicked(number)
    }

    override fun onBackspaceButtonClicked() {
        passCodeViewModel.onBackspaceClicked()
    }

    private fun subscribeToViewModel() {
        passCodeViewModel.getTimeToUnlockLiveData.observe(this, Event.EventObserver {
            binding.lockTime.text = getString(R.string.lock_time_try_again, it)
        })
        passCodeViewModel.getFinishedTimeToUnlockLiveData.observe(this, Event.EventObserver {
            binding.lockTime.visibility = View.INVISIBLE
            for (editText: EditText? in passCodeEditTexts) {
                editText?.isEnabled = true
            }
            passCodeEditTexts.first()?.requestFocus()
        })

        passCodeViewModel.status.observe(this) { status ->
            when (status.action) {
                PasscodeAction.CHECK -> {
                    when (status.type) {
                        PasscodeType.OK -> actionCheckOk()
                        PasscodeType.MIGRATION -> actionCheckMigration()
                        else -> actionCheckError()
                    }
                }
                PasscodeAction.REMOVE -> {
                    when (status.type) {
                        PasscodeType.OK -> actionRemoveOk()
                        else -> actionRemoveError()
                    }
                }
                PasscodeAction.CREATE -> {
                    when (status.type) {
                        PasscodeType.NO_CONFIRM -> actionCreateNoConfirm()
                        PasscodeType.CONFIRM -> actionCreateConfirm()
                        else -> actionCreateError()
                    }
                }
            }
        }

        passCodeViewModel.passcode.observe(this) { passcode ->
            if (passcode.isNotEmpty()) {
                passCodeEditTexts[passcode.length - 1]?.apply {
                    text = Editable.Factory.getInstance().newEditable(passcode.last().toString())
                    isEnabled = false
                }
            }

            if (passcode.length < numberOfPasscodeDigits) {

                passCodeEditTexts[passcode.length]?.apply {
                    isEnabled = true
                    setText("")
                    requestFocus()
                }
            }
        }
    }

    private fun actionCheckOk() {

        binding.error.visibility = View.INVISIBLE

        PassCodeManager.onActivityStopped(this)
        finish()
    }

    private fun actionCheckMigration() {
        binding.error.visibility = View.INVISIBLE

        val intent = Intent(baseContext, PassCodeActivity::class.java)
        intent.apply {
            action = ACTION_CREATE
            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRAS_MIGRATION, true)
        }
        startActivity(intent)

        PassCodeManager.onActivityStopped(this)
        finish()
    }

    private fun actionCheckError() {
        showErrorAndRestart(
            errorMessage = R.string.pass_code_wrong, headerMessage = getString(R.string.pass_code_enter_pass_code),
            explanationVisibility = View.INVISIBLE
        )
        if (passCodeViewModel.getNumberOfAttempts() >= NUM_ATTEMPTS_WITHOUT_TIMER) {
            lockScreen()
        }
    }

    private fun actionRemoveOk() {
        val resultIntent = Intent()
        setResult(RESULT_OK, resultIntent)
        notifyDocumentsProviderRoots(applicationContext)
        finish()
    }

    private fun actionRemoveError() {
        showErrorAndRestart(
            errorMessage = R.string.pass_code_wrong, headerMessage = getString(R.string.pass_code_enter_pass_code),
            explanationVisibility = View.INVISIBLE
        )
    }

    private fun actionCreateNoConfirm() {
        binding.error.visibility = View.INVISIBLE
        requestPassCodeConfirmation()
    }

    private fun actionCreateConfirm() {

        if (intent.extras?.getBoolean(EXTRAS_MIGRATION) == true) passCodeViewModel.setMigrationRequired(false)
        savePassCodeAndExit()
    }

    private fun actionCreateError() {
        val headerMessage = if (intent.extras?.getBoolean(EXTRAS_MIGRATION) == true) getString(
            R.string.pass_code_configure_your_pass_code_migration,
            passCodeViewModel.getNumberOfPassCodeDigits()
        )
        else getString(R.string.pass_code_configure_your_pass_code)
        showErrorAndRestart(
            errorMessage = R.string.pass_code_mismatch, headerMessage = headerMessage, explanationVisibility = View.VISIBLE
        )
    }

    private fun lockScreen() {
        val timeToUnlock = passCodeViewModel.getTimeToUnlockLeft()
        if (timeToUnlock > 0) {
            binding.lockTime.visibility = View.VISIBLE
            for (editText: EditText? in passCodeEditTexts) {
                editText?.isEnabled = false
            }
            passCodeViewModel.initUnlockTimer()
        }
    }

    private fun showErrorAndRestart(
        errorMessage: Int, headerMessage: String,
        explanationVisibility: Int
    ) {
        binding.error.setText(errorMessage)
        binding.error.visibility = View.VISIBLE
        binding.header.text = headerMessage
        binding.explanation.visibility = explanationVisibility
        clearBoxes()
    }


    private fun requestPassCodeConfirmation() {
        clearBoxes()
        binding.header.setText(R.string.pass_code_reenter_your_pass_code)
        binding.explanation.visibility = View.INVISIBLE
        confirmingPassCode = true
    }


    private fun clearBoxes() {
        for (passCodeEditText in passCodeEditTexts) {
            passCodeEditText?.apply {
                isEnabled = true
                setText("")
            }
        }
        passCodeEditTexts.first()?.requestFocus()
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.repeatCount == 0) {
            if ((ACTION_CREATE == intent.action &&
                        intent.extras?.getBoolean(EXTRAS_LOCK_ENFORCED) != true) ||
                ACTION_REMOVE == intent.action
            ) {
                finish()
            } // else, do nothing, but report that the key was consumed to stay alive
            return true
        }
        return super.onKeyDown(keyCode, event)
    }


    private fun savePassCodeAndExit() {
        setResult(RESULT_OK, resultIntent)
        notifyDocumentsProviderRoots(applicationContext)
        if (biometricViewModel.isBiometricLockAvailable()) {
            showBiometricDialog(this)
        } else {
            PassCodeManager.onActivityStopped(this)
            finish()
        }
    }

    override fun onOptionSelected(optionSelected: BiometricStatus) {
        when (optionSelected) {
            BiometricStatus.ENABLED_BY_USER -> {
                passCodeViewModel.setBiometricsState(enabled = true)
            }
            BiometricStatus.DISABLED_BY_USER -> {
                passCodeViewModel.setBiometricsState(enabled = false)
            }
        }
        PassCodeManager.onActivityStopped(this)
        finish()
    }

    private fun getPasscodeAction(action: String?): PasscodeAction {
        when (action) {
            ACTION_REMOVE -> {
                return PasscodeAction.REMOVE
            }
            ACTION_CREATE -> {
                return PasscodeAction.CREATE
            }
            else -> {
                return PasscodeAction.CHECK
            }
        }
    }

    companion object {
        const val ACTION_CREATE = "ACTION_REQUEST_WITH_RESULT"
        const val ACTION_REMOVE = "ACTION_CHECK_WITH_RESULT"
        const val ACTION_CHECK = "ACTION_CHECK"

        const val PREFERENCE_SET_PASSCODE = "set_pincode"
        const val PREFERENCE_PASSCODE = "PrefPinCode"
        const val PREFERENCE_MIGRATION_REQUIRED = "PrefMigrationRequired"

        const val PREFERENCE_PASSCODE_D = "PrefPinCode"

        const val EXTRAS_MIGRATION = "PASSCODE_MIGRATION"
        const val PASSCODE_MIN_LENGTH = 4

        private const val NUM_ATTEMPTS_WITHOUT_TIMER = 3

    }
}
