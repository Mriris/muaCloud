package com.owncloud.android.presentation.files.createfolder

import android.app.Dialog
import android.os.Bundle
import android.view.WindowManager
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputLayout
import com.owncloud.android.R
import com.owncloud.android.domain.files.model.OCFile
import com.owncloud.android.utils.PreferenceUtils


class CreateFolderDialogFragment : DialogFragment() {
    private lateinit var parentFolder: OCFile
    private lateinit var createFolderListener: CreateFolderListener
    private var isButtonEnabled: Boolean = false
    private val MAX_FILENAME_LENGTH = 223

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        if (savedInstanceState != null) {
            isButtonEnabled = savedInstanceState.getBoolean(IS_BUTTON_ENABLED_FLAG_KEY)
        }

        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.edit_box_dialog, null)

        view.filterTouchesWhenObscured = PreferenceUtils.shouldDisallowTouchesWithOtherVisibleWindows(context)
        val coordinatorLayout: CoordinatorLayout = requireActivity().findViewById(R.id.coordinator_layout)

        coordinatorLayout.filterTouchesWhenObscured =
            PreferenceUtils.shouldDisallowTouchesWithOtherVisibleWindows(context)

        val inputText: EditText = view.findViewById(R.id.user_input)
        val inputLayout: TextInputLayout = view.findViewById(R.id.edit_box_input_text_layout)
        var error: String? = null

        inputText.requestFocus()

        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(view)
            .setPositiveButton(android.R.string.ok) { dialog, _ ->
                createFolderListener.onFolderNameSet(
                    newFolderName = inputText.text.toString(),
                    parentFolder = parentFolder
                )
                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .setTitle(R.string.uploader_info_dirname)
        val alertDialog = builder.create()

        alertDialog.setOnShowListener {
            val okButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            okButton.isEnabled = isButtonEnabled

            okButton.setOnClickListener {
                var fileName: String = inputText.text.toString()
                createFolderListener.onFolderNameSet(fileName, parentFolder)
                dialog?.dismiss()
            }
        }

        inputText.doOnTextChanged { text, _, _, _ ->
            val okButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)

            if (text.isNullOrBlank()) {
                okButton.isEnabled = false
                error = getString(R.string.uploader_upload_text_dialog_filename_error_empty)
            } else if (text.length > MAX_FILENAME_LENGTH) {
                error = String.format(
                    getString(R.string.uploader_upload_text_dialog_filename_error_length_max),
                    MAX_FILENAME_LENGTH
                )
            } else if (forbiddenChars.any { text.contains(it) }) {
                error = getString(R.string.filename_forbidden_characters)
            } else {
                okButton.isEnabled = true
                error = null
                inputLayout.error = error
            }

            if (error != null) {
                okButton.isEnabled = false
                inputLayout.error = error
            }
        }


        alertDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        return alertDialog
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(IS_BUTTON_ENABLED_FLAG_KEY, isButtonEnabled)
    }

    interface CreateFolderListener {
        fun onFolderNameSet(newFolderName: String, parentFolder: OCFile)
    }

    companion object {
        const val CREATE_FOLDER_FRAGMENT = "CREATE_FOLDER_FRAGMENT"
        private const val IS_BUTTON_ENABLED_FLAG_KEY = "IS_BUTTON_ENABLED_FLAG_KEY"
        private val forbiddenChars = listOf('/', '\\')


        @JvmStatic
        fun newInstance(parent: OCFile, listener: CreateFolderListener): CreateFolderDialogFragment {
            return CreateFolderDialogFragment().apply {
                createFolderListener = listener
                parentFolder = parent
            }
        }
    }
}
