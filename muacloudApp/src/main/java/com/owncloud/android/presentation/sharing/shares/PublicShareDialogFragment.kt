

package com.owncloud.android.presentation.sharing.shares

import android.accounts.Account
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.CompoundButton
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.DialogFragment
import com.owncloud.android.MainApp.Companion.appContext
import com.owncloud.android.R
import com.owncloud.android.databinding.SharePublicDialogBinding
import com.owncloud.android.domain.capabilities.model.CapabilityBooleanType
import com.owncloud.android.domain.capabilities.model.OCCapability
import com.owncloud.android.domain.exceptions.UnhandledHttpCodeException
import com.owncloud.android.domain.files.model.OCFile
import com.owncloud.android.domain.sharing.shares.model.OCShare
import com.owncloud.android.domain.utils.Event.EventObserver
import com.owncloud.android.extensions.avoidScreenshotsIfNeeded
import com.owncloud.android.extensions.parseError
import com.owncloud.android.extensions.showMessageInSnackbar
import com.owncloud.android.lib.resources.shares.RemoteShare
import com.owncloud.android.lib.resources.status.OwnCloudVersion
import com.owncloud.android.presentation.capabilities.CapabilityViewModel
import com.owncloud.android.presentation.common.UIResult
import com.owncloud.android.presentation.sharing.ShareFragmentListener
import com.owncloud.android.presentation.sharing.ShareViewModel
import com.owncloud.android.presentation.sharing.generatePassword
import com.owncloud.android.ui.dialog.ExpirationDatePickerDialogFragment
import com.owncloud.android.utils.DateUtils
import com.owncloud.android.utils.PreferenceUtils
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import timber.log.Timber
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date

class PublicShareDialogFragment : DialogFragment() {


    private var file: OCFile? = null


    private var account: Account? = null


    private var publicShare: OCShare? = null


    private var listener: ShareFragmentListener? = null


    private var capabilities: OCCapability? = null


    private var onPasswordInteractionListener: OnPasswordInteractionListener? = null


    private var onExpirationDateInteractionListener: OnExpirationDateInteractionListener? = null

    private val isSharedFolder: Boolean
        get() = file?.isFolder == true || publicShare?.isFolder == true

    private val isPasswordVisible: Boolean
        get() = view != null && binding.shareViaLinkPasswordValue.inputType and
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

    private// Parse expiration date and convert it to milliseconds

    val expirationDateValueInMillis: Long
        get() {
            var publicLinkExpirationDateInMillis: Long = -1
            val expirationDate = binding.shareViaLinkExpirationValue.text.toString()
            if (expirationDate.isNotEmpty()) {
                try {
                    publicLinkExpirationDateInMillis =
                        ExpirationDatePickerDialogFragment.getDateFormat().parse(expirationDate).time
                } catch (e: ParseException) {
                    Timber.e(e, "Error reading expiration date from input field")
                }

            }
            return publicLinkExpirationDateInMillis
        }


    private val imposedExpirationDate: Long
        get() = if (capabilities?.filesSharingPublicExpireDateEnforced == CapabilityBooleanType.TRUE) {
            DateUtils.addDaysToDate(
                Date(),
                capabilities?.filesSharingPublicExpireDateDays!!
            )
                .time
        } else -1

    private val capabilityViewModel: CapabilityViewModel by viewModel {
        parametersOf(
            account?.name
        )
    }

    private val shareViewModel: ShareViewModel by viewModel {
        parametersOf(
            file?.remotePath,
            account?.name
        )
    }

    private var _binding: SharePublicDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            file = it.getParcelable(ARG_FILE)
            account = it.getParcelable(ARG_ACCOUNT)
            publicShare = it.getParcelable(ARG_SHARE)
        }

        check(file != null || publicShare != null) {
            "Both ARG_FILE and ARG_SHARE cannot be NULL"
        }

        setStyle(STYLE_NO_TITLE, 0)
    }

    private fun updating(): Boolean = publicShare != null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SharePublicDialogBinding.inflate(inflater, container, false)
        return binding.root.apply {

            filterTouchesWhenObscured = PreferenceUtils.shouldDisallowTouchesWithOtherVisibleWindows(context)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        if (savedInstanceState != null) {
            val expirationDate = savedInstanceState.getString(KEY_EXPIRATION_DATE)
            if (!expirationDate.isNullOrEmpty()) {
                binding.shareViaLinkExpirationValue.isVisible = true
                binding.shareViaLinkExpirationValue.text = expirationDate
            }
        }

        initTitleAndLabels()
        initPasswordListener()
        initExpirationListener()
        initPasswordFocusChangeListener()
        initPasswordChangeInputListener()
        initPasswordToggleListener()

        binding.saveButton.setOnClickListener { onSaveShareSetting() }
        binding.cancelButton.setOnClickListener { dismiss() }

        binding.copyPasswordButton.setOnClickListener {
            val passwordText = binding.shareViaLinkPasswordValue.text.toString()
            val clipboard = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Public link", passwordText)
            clipboard.setPrimaryClip(clip)
            showMessageInSnackbar(getString(R.string.clipboard_text_copied))
        }

        dialog?.avoidScreenshotsIfNeeded()
    }

    private fun initTitleAndLabels() {
        if (updating()) {
            binding.publicShareDialogTitle.setText(R.string.share_via_link_edit_title)
            binding.shareViaLinkNameValue.setText(publicShare?.name)

            when (publicShare?.permissions) {
                RemoteShare.CREATE_PERMISSION_FLAG
                        or RemoteShare.DELETE_PERMISSION_FLAG
                        or RemoteShare.UPDATE_PERMISSION_FLAG
                        or RemoteShare.READ_PERMISSION_FLAG ->
                    binding.shareViaLinkEditPermissionReadAndWrite.isChecked = true

                RemoteShare.CREATE_PERMISSION_FLAG -> binding.shareViaLinkEditPermissionUploadFiles.isChecked = true
                else -> binding.shareViaLinkEditPermissionReadOnly.isChecked = true
            }

            if (publicShare?.isPasswordProtected == true) {
                setPasswordSwitchChecked()
                binding.shareViaLinkPasswordValue.isVisible = true
                binding.shareViaLinkPasswordValue.hint = getString(R.string.share_via_link_default_password)
            }

            if (publicShare?.expirationDate != 0L) {
                setExpirationDateSwitchChecked()
                val formattedDate = ExpirationDatePickerDialogFragment.getDateFormat().format(
                    Date(publicShare?.expirationDate!!)
                )
                binding.shareViaLinkExpirationValue.isVisible = true
                binding.shareViaLinkExpirationValue.text = formattedDate
            }

        } else {
            binding.shareViaLinkNameValue.setText(arguments?.getString(ARG_DEFAULT_LINK_NAME, ""))
        }
    }

    private fun onSaveShareSetting() {

        val publicLinkName = binding.shareViaLinkNameValue.text.toString()
        var publicLinkPassword: String? = binding.shareViaLinkPasswordValue.text.toString()
        val publicLinkExpirationDateInMillis = expirationDateValueInMillis

        val publicLinkPermissions: Int
        val publicUploadPermission: Boolean

        when (binding.shareViaLinkEditPermissionGroup.checkedRadioButtonId) {
            R.id.shareViaLinkEditPermissionUploadFiles -> {
                publicLinkPermissions = RemoteShare.CREATE_PERMISSION_FLAG
                publicUploadPermission = true
            }

            R.id.shareViaLinkEditPermissionReadAndWrite -> {
                publicLinkPermissions = (RemoteShare.CREATE_PERMISSION_FLAG
                        or RemoteShare.DELETE_PERMISSION_FLAG
                        or RemoteShare.UPDATE_PERMISSION_FLAG
                        or RemoteShare.READ_PERMISSION_FLAG)
                publicUploadPermission = true
            }

            R.id.shareViaLinkEditPermissionReadOnly -> {
                publicLinkPermissions = RemoteShare.READ_PERMISSION_FLAG
                publicUploadPermission = false
            }

            else -> {
                publicLinkPermissions = RemoteShare.READ_PERMISSION_FLAG
                publicUploadPermission = false
            }
        }

        if (!updating()) { // Creating a new public share
            shareViewModel.insertPublicShare(
                file?.remotePath!!,
                publicLinkPermissions,
                publicLinkName,
                publicLinkPassword!!,
                publicLinkExpirationDateInMillis,
                account?.name!!
            )
        } else { // Updating an existing public share
            if (!binding.shareViaLinkPasswordSwitch.isChecked) {
                publicLinkPassword = ""
            } else if (binding.shareViaLinkPasswordValue.text.isEmpty()) {

                publicLinkPassword = null
            }
            shareViewModel.updatePublicShare(
                publicShare?.remoteId!!,
                publicLinkName,
                publicLinkPassword,
                publicLinkExpirationDateInMillis,
                publicLinkPermissions,
                account?.name!!
            )
        }
    }

    private fun initPasswordFocusChangeListener() {
        binding.shareViaLinkPasswordValue.setOnFocusChangeListener { v: View, hasFocus: Boolean ->
            if (v.id == R.id.shareViaLinkPasswordValue) {
                onPasswordFocusChanged(hasFocus)
            }
        }
    }

    private fun initPasswordChangeInputListener() {
        binding.shareViaLinkPasswordValue.doOnTextChanged { text, _, _, _ ->
            capabilities?.passwordPolicy?.let { passwordPolicy ->
                updateRequirementsPasswordPolicy(text.toString(), passwordPolicy)
            } ?: handleNullPasswordPolicy()
        }
    }

    private fun initPasswordToggleListener() {
        binding.shareViaLinkPasswordValue.setOnTouchListener(object : RightDrawableOnTouchListener() {
            override fun onDrawableTouch(event: MotionEvent): Boolean {
                if (event.action == MotionEvent.ACTION_UP) {
                    onViewPasswordClick()
                }
                return true
            }
        })
    }

    private fun handleNullPasswordPolicy() {
        if (binding.shareViaLinkPasswordSwitch.isChecked) {
            binding.saveButton.isEnabled = binding.shareViaLinkPasswordValue.text.isNotBlank()
        }
    }

    private fun updateRequirementsPasswordPolicy(password: String, passwordPolicy: OCCapability.PasswordPolicy) {

        var hasMinCharacters = true
        var hasMaxCharacters = true
        var hasUpperCase = true
        var hasLowerCase = true
        var hasSpecialCharacter = true
        var hasDigit = true

        binding.shareViaLinkPasswordPolicyIntro.isVisible = true
        binding.shareViaLinkPasswordPolicyIntro.text = getString(
            R.string.password_policy_intro
        )

        passwordPolicy.minCharacters?.let { minCharacters ->
            if (minCharacters > 0) {
                hasMinCharacters = password.length >= minCharacters
                binding.shareViaLinkPasswordPolicyMinCharactersText.text = getString(
                    R.string.password_policy_min_characters, passwordPolicy.minCharacters
                )
                binding.shareViaLinkPasswordPolicyMinCharacters.isVisible = true
                handleRequirementCheckedOrWarning(
                    hasRequirement = hasMinCharacters,
                    textViewIcon = binding.shareViaLinkPasswordPolicyMinCharactersIcon,
                    textView = binding.shareViaLinkPasswordPolicyMinCharactersText
                )
            }
        }

        passwordPolicy.maxCharacters?.let { maxCharacters ->
            if (maxCharacters > 0) {
                hasMaxCharacters = password.length <= maxCharacters
                binding.shareViaLinkPasswordPolicyMaxCharactersText.text = getString(
                    R.string.password_policy_max_characters, passwordPolicy.maxCharacters
                )
                binding.shareViaLinkPasswordPolicyMaxCharacters.isVisible = true
                handleRequirementCheckedOrWarning(
                    hasRequirement = hasMaxCharacters,
                    textViewIcon = binding.shareViaLinkPasswordPolicyMaxCharactersIcon,
                    textView = binding.shareViaLinkPasswordPolicyMaxCharactersText
                )
            }
        }

        passwordPolicy.minUppercaseCharacters?.let { minUppercaseCharacters ->
            if (minUppercaseCharacters > 0) {
                hasUpperCase = password.count { it.isUpperCase() } >= minUppercaseCharacters
                binding.shareViaLinkPasswordPolicyUpperCaseCharactersText.text = getString(
                    R.string.password_policy_uppercase_characters, passwordPolicy.minUppercaseCharacters
                )
                binding.shareViaLinkPasswordPolicyUpperCaseCharacters.isVisible = true
                handleRequirementCheckedOrWarning(
                    hasRequirement = hasUpperCase,
                    textViewIcon = binding.shareViaLinkPasswordPolicyUpperCaseCharactersIcon,
                    textView = binding.shareViaLinkPasswordPolicyUpperCaseCharactersText
                )
            }
        }

        passwordPolicy.minLowercaseCharacters?.let { minLowercaseCharacters ->
            if (minLowercaseCharacters > 0) {
                hasLowerCase = password.count { it.isLowerCase() } >= minLowercaseCharacters
                binding.shareViaLinkPasswordPolicyLowerCaseCharactersText.text = getString(
                    R.string.password_policy_lowercase_characters, passwordPolicy.minLowercaseCharacters
                )
                binding.shareViaLinkPasswordPolicyLowerCaseCharacters.isVisible = true
                handleRequirementCheckedOrWarning(
                    hasRequirement = hasLowerCase,
                    textViewIcon = binding.shareViaLinkPasswordPolicyLowerCaseCharactersIcon,
                    textView = binding.shareViaLinkPasswordPolicyLowerCaseCharactersText
                )
            }
        }

        passwordPolicy.minSpecialCharacters?.let { minSpecialCharacters ->
            if (minSpecialCharacters > 0) {
                hasSpecialCharacter = password.count { SPECIALS_CHARACTERS.contains(it) } >= minSpecialCharacters
                binding.shareViaLinkPasswordPolicyMinSpecialCharactersText.text = getString(
                    R.string.password_policy_min_special_character, passwordPolicy.minSpecialCharacters, SPECIALS_CHARACTERS
                )
                binding.shareViaLinkPasswordPolicyMinSpecialCharacters.isVisible = true
                handleRequirementCheckedOrWarning(
                    hasRequirement = hasSpecialCharacter,
                    textViewIcon = binding.shareViaLinkPasswordPolicyMinSpecialCharactersIcon,
                    textView = binding.shareViaLinkPasswordPolicyMinSpecialCharactersText
                )
            }
        }

        passwordPolicy.minDigits?.let { minDigits ->
            if (minDigits > 0) {
                hasDigit = password.count { it.isDigit() } >= minDigits
                binding.shareViaLinkPasswordPolicyMinDigitsText.text = getString(
                    R.string.password_policy_min_digits, passwordPolicy.minDigits
                )
                binding.shareViaLinkPasswordPolicyMinDigits.isVisible = true
                handleRequirementCheckedOrWarning(
                    hasRequirement = hasDigit,
                    textViewIcon = binding.shareViaLinkPasswordPolicyMinDigitsIcon,
                    textView = binding.shareViaLinkPasswordPolicyMinDigitsText
                )
            }
        }

        val allConditionsCheck = hasMinCharacters && hasUpperCase && hasLowerCase && hasDigit && hasSpecialCharacter && hasMaxCharacters
        binding.saveButton.isEnabled = allConditionsCheck
        enableCopyPasswordButton(allConditionsCheck)
    }

    private fun handleRequirementCheckedOrWarning(hasRequirement: Boolean, textViewIcon: TextView, textView: TextView) {
        if (hasRequirement) {
            requirementChecked(textViewIcon, textView)
        } else {
            requirementWarning(textViewIcon, textView)
        }
    }

    private fun requirementChecked(textViewIcon: TextView, textView: TextView) {
        textViewIcon.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_check_password_policy, 0, 0, 0)
        textView.setTextColor(ContextCompat.getColor(appContext, R.color.success))
    }

    private fun requirementWarning(textViewIcon: TextView, textView: TextView) {
        textViewIcon.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_cross_warning_password_policy, 0, 0, 0)
        textView.setTextColor(ContextCompat.getColor(appContext, R.color.warning))
    }

    private fun enableCopyPasswordButton(enable: Boolean) {
        binding.copyPasswordButton.apply {
            isEnabled = enable
            setTextColor(
                if (enable) resources.getColor(R.color.primary_button_background_color, null)
                else resources.getColor(R.color.grey, null)
            )
        }
    }

    private abstract class RightDrawableOnTouchListener : View.OnTouchListener {

        private val fuzz = 75


        override fun onTouch(view: View, event: MotionEvent): Boolean {
            var rightDrawable: Drawable? = null
            if (view is TextView) {
                val drawables = view.compoundDrawables
                if (drawables.size > 2) {
                    rightDrawable = drawables[2]
                }
            }
            if (rightDrawable != null) {
                val x = event.x.toInt()
                val y = event.y.toInt()
                val bounds = rightDrawable.bounds
                if (x >= view.right - bounds.width() - fuzz && x <= view.right - view.paddingRight + fuzz && y >= view.paddingTop - fuzz && y <= view.height - view.paddingBottom + fuzz) {

                    return onDrawableTouch(event)
                }
            }
            return false
        }

        abstract fun onDrawableTouch(event: MotionEvent): Boolean
    }


    private fun onPasswordFocusChanged(hasFocus: Boolean) {
        if (hasFocus) {
            showViewPasswordButton()
        } else {
            hidePassword()
            hidePasswordButton()
        }

    }


    fun onViewPasswordClick() {
        if (view != null) {
            if (isPasswordVisible) {
                hidePassword()
            } else {
                showPassword()
            }
            binding.shareViaLinkPasswordValue.setSelection(
                binding.shareViaLinkPasswordValue.selectionStart,
                binding.shareViaLinkPasswordValue.selectionEnd
            )
        }
    }

    private fun showViewPasswordButton() {
        val drawable = if (isPasswordVisible)
            R.drawable.ic_view_black
        else
            R.drawable.ic_hide_black
        binding.shareViaLinkPasswordValue.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawable, 0)
    }

    private fun hidePasswordButton() {
        binding.shareViaLinkPasswordValue.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
    }

    private fun showPassword() {
        binding.shareViaLinkPasswordValue.inputType =
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        showViewPasswordButton()
    }

    private fun hidePassword() {
        binding.shareViaLinkPasswordValue.inputType =
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        showViewPasswordButton()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        observeCapabilities()
        observePublicShareCreation()
        observePublicShareEdition()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_EXPIRATION_DATE, binding.shareViaLinkExpirationValue.text.toString())
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = activity as ShareFragmentListener?
        } catch (e: IllegalStateException) {
            throw IllegalStateException(activity?.toString() + " must implement OnShareFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    private fun observeCapabilities() {
        capabilityViewModel.capabilities.observe(this) { event ->
            when (val uiResult = event.peekContent()) {
                is UIResult.Success -> {
                    updateCapabilities(uiResult.data)
                    listener?.dismissLoading()
                }

                is UIResult.Error -> {
                }

                is UIResult.Loading -> {
                }
            }
        }
    }

    private fun observePublicShareCreation() {
        shareViewModel.publicShareCreationStatus.observe(this, EventObserver { uiResult ->
            when (uiResult) {
                is UIResult.Success -> {
                    dismiss()
                }

                is UIResult.Error -> {
                    showError(getString(R.string.share_link_file_error), uiResult.error)
                    listener?.dismissLoading()
                }

                is UIResult.Loading -> {
                    listener?.showLoading()
                }
            }
        })
    }

    private fun observePublicShareEdition() {
        shareViewModel.publicShareEditionStatus.observe(this, EventObserver { uiResult ->
            when (uiResult) {
                is UIResult.Success -> {
                    dismiss()
                }

                is UIResult.Error -> {
                    showError(getString(R.string.update_link_file_error), uiResult.error)
                    listener?.dismissLoading()
                }

                is UIResult.Loading -> {
                    listener?.showLoading()
                }
            }
        })
    }


    private fun initPasswordListener() {
        onPasswordInteractionListener = OnPasswordInteractionListener()
        binding.shareViaLinkPasswordSwitch.setOnCheckedChangeListener(onPasswordInteractionListener)
    }


    private inner class OnPasswordInteractionListener : CompoundButton.OnCheckedChangeListener {

        override fun onCheckedChanged(switchView: CompoundButton, isChecked: Boolean) {
            if (isChecked) {
                binding.shareViaLinkPasswordValue.isVisible = true
                binding.shareViaLinkPasswordValue.requestFocus()
                binding.saveButton.isEnabled = false
                capabilities?.passwordPolicy?.let {
                    binding.layoutPasswordGeneratorButtons.isVisible = true
                }

                val mgr = activity?.getSystemService(
                    Context.INPUT_METHOD_SERVICE
                ) as InputMethodManager?
                mgr?.showSoftInput(binding.shareViaLinkPasswordValue, InputMethodManager.SHOW_IMPLICIT)

            } else {
                binding.shareViaLinkPasswordValue.isVisible = false
                binding.saveButton.isEnabled = true
                binding.shareViaLinkPasswordValue.text?.clear()
                capabilities?.passwordPolicy?.let {
                    binding.layoutPasswordGeneratorButtons.isVisible = false
                }
            }
        }
    }


    private fun initExpirationListener() {
        onExpirationDateInteractionListener = OnExpirationDateInteractionListener()
        binding.shareViaLinkExpirationSwitch.setOnCheckedChangeListener(onExpirationDateInteractionListener)
        binding.shareViaLinkExpirationLabel.setOnClickListener(onExpirationDateInteractionListener)
        binding.shareViaLinkExpirationValue.setOnClickListener(onExpirationDateInteractionListener)
    }


    private inner class OnExpirationDateInteractionListener : CompoundButton.OnCheckedChangeListener, View.OnClickListener,
        ExpirationDatePickerDialogFragment.DatePickerFragmentListener {


        override fun onCheckedChanged(switchView: CompoundButton, isChecked: Boolean) {
            if (!isResumed) {


                return
            }

            if (isChecked) {

                val dialog = ExpirationDatePickerDialogFragment.newInstance(
                    expirationDateValueInMillis, imposedExpirationDate
                )
                dialog.setDatePickerListener(this)
                dialog.show(
                    requireActivity().supportFragmentManager, ExpirationDatePickerDialogFragment.DATE_PICKER_DIALOG
                )
            } else {
                binding.shareViaLinkExpirationValue.visibility = View.INVISIBLE
                binding.shareViaLinkExpirationValue.text = ""
            }
        }


        override fun onClick(expirationView: View) {

            val dialog = ExpirationDatePickerDialogFragment.newInstance(
                expirationDateValueInMillis, imposedExpirationDate
            )
            dialog.setDatePickerListener(this)
            dialog.show(
                requireActivity().supportFragmentManager, ExpirationDatePickerDialogFragment.DATE_PICKER_DIALOG
            )
        }


        override fun onDateSet(date: String) {
            binding.shareViaLinkExpirationValue.isVisible = true
            binding.shareViaLinkExpirationValue.text = date
        }

        override fun onCancelDatePicker() {

            if (binding.shareViaLinkExpirationSwitch.isChecked && binding.shareViaLinkExpirationValue.text.isNullOrBlank()) {
                binding.shareViaLinkExpirationSwitch.isChecked = false
            }
        }
    }

    private fun updateCapabilities(capabilities: OCCapability?) {
        this.capabilities = capabilities
        updateInputFormAccordingToServerCapabilities()
    }


    private fun updateInputFormAccordingToServerCapabilities() {
        val serverVersion = capabilities?.versionString?.let {
            OwnCloudVersion(it)
        }

        dialog?.window?.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )

        if (capabilities?.filesSharingPublicUpload == CapabilityBooleanType.TRUE && isSharedFolder) {
            binding.shareViaLinkEditPermissionGroup.isVisible = true
        }





        if (!(isSharedFolder && serverVersion?.isPublicSharingWriteOnlySupported == true && capabilities?.filesSharingPublicSupportsUploadOnly == CapabilityBooleanType.TRUE && capabilities?.filesSharingPublicUpload == CapabilityBooleanType.TRUE)) {
            binding.shareViaLinkEditPermissionGroup.isVisible = false
        }

        if (!updating() && capabilities?.filesSharingPublicExpireDateDays ?: 0 > 0) {
            setExpirationDateSwitchChecked()

            val formattedDate = SimpleDateFormat.getDateInstance().format(
                DateUtils.addDaysToDate(
                    Date(), capabilities?.filesSharingPublicExpireDateDays!!
                )
            )

            binding.shareViaLinkExpirationValue.apply {
                isVisible = true
                text = formattedDate
            }
        }

        if (capabilities?.filesSharingPublicExpireDateEnforced == CapabilityBooleanType.TRUE) {
            binding.shareViaLinkExpirationLabel.text = getString(R.string.share_via_link_expiration_date_enforced_label)
            binding.shareViaLinkExpirationSwitch.isVisible = false
            binding.shareViaLinkExpirationExplanationLabel.isVisible = true
            binding.shareViaLinkExpirationExplanationLabel.text = getString(
                R.string.share_via_link_expiration_date_explanation_label, capabilities?.filesSharingPublicExpireDateDays
            )
        }

        if (binding.shareViaLinkEditPermissionReadOnly.isChecked && capabilities?.filesSharingPublicPasswordEnforcedReadOnly == CapabilityBooleanType.TRUE || binding.shareViaLinkEditPermissionReadAndWrite.isChecked && capabilities?.filesSharingPublicPasswordEnforcedReadWrite == CapabilityBooleanType.TRUE || binding.shareViaLinkEditPermissionUploadFiles.isChecked && capabilities?.filesSharingPublicPasswordEnforcedUploadOnly == CapabilityBooleanType.TRUE) {
            setPasswordEnforced()
        }

        binding.shareViaLinkEditPermissionGroup.setOnCheckedChangeListener { _, checkedId ->
            binding.publicLinkErrorMessage.isVisible = false

            if (checkedId == binding.shareViaLinkEditPermissionReadOnly.id) {
                if (capabilities?.filesSharingPublicPasswordEnforcedReadOnly == CapabilityBooleanType.TRUE) {
                    setPasswordEnforced()
                } else {
                    setPasswordNotEnforced()
                }
            } else if (checkedId == binding.shareViaLinkEditPermissionReadAndWrite.id) {
                if (capabilities?.filesSharingPublicPasswordEnforcedReadWrite == CapabilityBooleanType.TRUE) {
                    setPasswordEnforced()
                } else {
                    setPasswordNotEnforced()
                }
            } else if (checkedId == binding.shareViaLinkEditPermissionUploadFiles.id) {
                if (capabilities?.filesSharingPublicPasswordEnforcedUploadOnly == CapabilityBooleanType.TRUE) {
                    setPasswordEnforced()
                } else {
                    setPasswordNotEnforced()
                }
            }
        }

        val hasPasswordEnforcedFor =
            capabilities?.filesSharingPublicPasswordEnforcedReadOnly == CapabilityBooleanType.TRUE || capabilities?.filesSharingPublicPasswordEnforcedReadWrite == CapabilityBooleanType.TRUE || capabilities?.filesSharingPublicPasswordEnforcedUploadOnly == CapabilityBooleanType.TRUE

        if (!hasPasswordEnforcedFor && capabilities?.filesSharingPublicPasswordEnforced == CapabilityBooleanType.TRUE) {
            setPasswordEnforced()
        }

        capabilities?.passwordPolicy?.let { passwordPolicy ->
            binding.generatePasswordButton.setOnClickListener {
                binding.shareViaLinkPasswordValue.setText(
                    generatePassword(
                        minChars = passwordPolicy.minCharacters,
                        maxChars = passwordPolicy.maxCharacters,
                        minDigitsChars = passwordPolicy.minDigits,
                        minLowercaseChars = passwordPolicy.minLowercaseCharacters,
                        minUppercaseChars = passwordPolicy.minUppercaseCharacters,
                        minSpecialChars = passwordPolicy.minSpecialCharacters,
                    )
                )
                showPassword()
            }
        }
    }

    private fun setPasswordNotEnforced() {
        binding.shareViaLinkPasswordLabel.text = getString(R.string.share_via_link_password_label)
        binding.shareViaLinkPasswordSwitch.isVisible = true
        if (!binding.shareViaLinkPasswordSwitch.isChecked) {
            binding.shareViaLinkPasswordValue.isVisible = false
            capabilities?.passwordPolicy?.let {
                binding.layoutPasswordGeneratorButtons.isVisible = false
            }
        }
    }

    private fun setPasswordEnforced() {
        binding.shareViaLinkPasswordLabel.text = getString(R.string.share_via_link_password_enforced_label)
        binding.shareViaLinkPasswordSwitch.isChecked = true
        binding.shareViaLinkPasswordSwitch.isVisible = false
        binding.shareViaLinkPasswordValue.isVisible = true
        capabilities?.passwordPolicy?.let {
            binding.layoutPasswordGeneratorButtons.isVisible = true
        }
    }


    private fun showError(genericErrorMessage: String, throwable: Throwable?) {
        if (throwable is UnhandledHttpCodeException) {
            binding.publicLinkErrorMessage.text = getString(R.string.password_policy_error_password_banned)
        } else {
            binding.publicLinkErrorMessage.text = throwable?.parseError(genericErrorMessage, resources)
        }
        binding.publicLinkErrorMessage.isVisible = true
        binding.saveButton.isEnabled = false
        enableCopyPasswordButton(false)
    }

    private fun setPasswordSwitchChecked() {
        binding.shareViaLinkPasswordSwitch.apply {
            setOnCheckedChangeListener(null)
            isChecked = true
            setOnCheckedChangeListener(onPasswordInteractionListener)
        }
    }

    private fun setExpirationDateSwitchChecked() {
        binding.shareViaLinkExpirationSwitch.setOnCheckedChangeListener(null)
        binding.shareViaLinkExpirationSwitch.isChecked = true
        binding.shareViaLinkExpirationSwitch.setOnCheckedChangeListener(onExpirationDateInteractionListener)
    }

    companion object {

        private const val ARG_FILE = "FILE"
        private const val ARG_SHARE = "SHARE"
        private const val ARG_ACCOUNT = "ACCOUNT"
        private const val ARG_DEFAULT_LINK_NAME = "DEFAULT_LINK_NAME"
        private const val KEY_EXPIRATION_DATE = "EXPIRATION_DATE"
        private const val SPECIALS_CHARACTERS = "!#$%&'()*+,-./:;<=>?@[\\]^_`{|}~"


        fun newInstanceToCreate(
            fileToShare: OCFile, account: Account, defaultLinkName: String
        ): PublicShareDialogFragment {
            val args = Bundle().apply {
                putParcelable(ARG_FILE, fileToShare)
                putParcelable(ARG_ACCOUNT, account)
                putString(ARG_DEFAULT_LINK_NAME, defaultLinkName)
            }

            return PublicShareDialogFragment().apply { arguments = args }
        }


        fun newInstanceToUpdate(
            fileToShare: OCFile, account: Account, publicShare: OCShare
        ): PublicShareDialogFragment {
            val args = Bundle().apply {
                putParcelable(ARG_FILE, fileToShare)
                putParcelable(ARG_ACCOUNT, account)
                putParcelable(ARG_SHARE, publicShare)
            }

            return PublicShareDialogFragment().apply { arguments = args }
        }
    }
}
