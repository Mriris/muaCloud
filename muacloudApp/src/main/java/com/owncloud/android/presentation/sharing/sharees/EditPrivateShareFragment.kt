

package com.owncloud.android.presentation.sharing.sharees

import android.accounts.Account
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.owncloud.android.R
import com.owncloud.android.databinding.EditShareLayoutBinding
import com.owncloud.android.domain.files.model.OCFile
import com.owncloud.android.domain.sharing.shares.model.OCShare
import com.owncloud.android.domain.sharing.shares.model.ShareType
import com.owncloud.android.domain.utils.Event.EventObserver
import com.owncloud.android.extensions.parseError
import com.owncloud.android.lib.resources.shares.RemoteShare
import com.owncloud.android.lib.resources.shares.SharePermissionsBuilder
import com.owncloud.android.presentation.common.UIResult
import com.owncloud.android.presentation.sharing.ShareFragmentListener
import com.owncloud.android.presentation.sharing.ShareViewModel
import com.owncloud.android.utils.PreferenceUtils
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import timber.log.Timber


class EditPrivateShareFragment : DialogFragment() {

        private var share: OCShare? = null

        private var file: OCFile? = null

        private var account: Account? = null


    private var listener: ShareFragmentListener? = null

        private var onPrivilegeChangeListener: CompoundButton.OnCheckedChangeListener? = null

    private val shareViewModel: ShareViewModel by viewModel {
        parametersOf(
            file?.remotePath,
            account?.name
        )
    }

    private var _binding: EditShareLayoutBinding? = null
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.v("onCreate")
        if (arguments != null) {
            file = arguments?.getParcelable(ARG_FILE)
            account = arguments?.getParcelable(ARG_ACCOUNT)
            share = savedInstanceState?.getParcelable(ARG_SHARE) ?: arguments?.getParcelable(ARG_SHARE)
            Timber.d("Share has id ${share?.id} remoteId ${share?.remoteId}")
        }

        setStyle(STYLE_NO_TITLE, 0)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Timber.v("onActivityCreated")

        refreshPrivateShare(share?.remoteId!!)
        observePrivateShareToEdit()

        observePrivateShareEdition()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = EditShareLayoutBinding.inflate(inflater, container, false)
        return binding.root.apply {

            filterTouchesWhenObscured = PreferenceUtils.shouldDisallowTouchesWithOtherVisibleWindows(context)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.editShareTitle.text = resources.getString(R.string.share_with_edit_title, share?.sharedWithDisplayName)

        refreshUiFromState()

        binding.closeButton.setOnClickListener { dismiss() }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            listener = activity as ShareFragmentListener?
        } catch (e: IllegalStateException) {
            throw IllegalStateException(requireActivity().toString() + " must implement OnShareFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()

        listener = null
    }


    private fun refreshUiFromState() {
        setPermissionsListening(false)

        val sharePermissions = share!!.permissions

        binding.canShareSwitch.isChecked = sharePermissions and RemoteShare.SHARE_PERMISSION_FLAG > 0

        val anyUpdatePermission = RemoteShare.CREATE_PERMISSION_FLAG or
                RemoteShare.UPDATE_PERMISSION_FLAG or
                RemoteShare.DELETE_PERMISSION_FLAG
        val canEdit = sharePermissions and anyUpdatePermission > 0
        binding.canEditSwitch.isChecked = canEdit

        if (file?.isFolder == true) {


            binding.canEditCreateCheckBox.apply {
                isChecked = sharePermissions and RemoteShare.CREATE_PERMISSION_FLAG > 0
                isVisible = canEdit
            }

            binding.canEditChangeCheckBox.apply {
                isChecked = sharePermissions and RemoteShare.UPDATE_PERMISSION_FLAG > 0
                isVisible = canEdit

            }
            binding.canEditDeleteCheckBox.apply {
                isChecked = sharePermissions and RemoteShare.DELETE_PERMISSION_FLAG > 0
                isVisible = canEdit
            }
        }

        setPermissionsListening(true)
    }


    private fun setPermissionsListening(enable: Boolean) {
        if (enable && onPrivilegeChangeListener == null) {
            onPrivilegeChangeListener = OnPrivilegeChangeListener()
        }
        val changeListener = if (enable) onPrivilegeChangeListener else null

        binding.canShareSwitch.setOnCheckedChangeListener(changeListener)
        binding.canEditSwitch.setOnCheckedChangeListener(changeListener)

        if (file?.isFolder == true) {
            binding.canEditCreateCheckBox.setOnCheckedChangeListener(changeListener)
            binding.canEditChangeCheckBox.setOnCheckedChangeListener(changeListener)
            binding.canEditDeleteCheckBox.setOnCheckedChangeListener(changeListener)
        }
    }


    private inner class OnPrivilegeChangeListener : CompoundButton.OnCheckedChangeListener {


        override fun onCheckedChanged(compound: CompoundButton, isChecked: Boolean) {
            if (!isResumed) {


                return
            }


            var subordinate: CompoundButton
            when (compound.id) {
                R.id.canShareSwitch -> {
                    Timber.v("canShareCheckBox toggled to $isChecked")
                    updatePermissionsToShare()
                }

                R.id.canEditSwitch -> {
                    Timber.v("canEditCheckBox toggled to $isChecked")

                    val isFederated = share?.shareType == ShareType.FEDERATED
                    if (file?.isFolder == true) {
                        if (isChecked) {
                            if (!isFederated) {

                                for (i in sSubordinateCheckBoxIds.indices) {

                                    subordinate = view!!.findViewById(sSubordinateCheckBoxIds[i])
                                    if (!isFederated) { // TODO delete when iOS is ready
                                        subordinate.visibility = View.VISIBLE
                                    }
                                    if (!subordinate.isChecked && !file!!.isSharedWithMe) {          // see (1)
                                        toggleDisablingListener(subordinate)
                                    }
                                }
                            } else {


                                subordinate = binding.canEditDeleteCheckBox
                                if (!subordinate.isChecked) {
                                    toggleDisablingListener(subordinate)
                                }
                            }
                        } else {
                            for (i in sSubordinateCheckBoxIds.indices) {

                                subordinate = view!!.findViewById(sSubordinateCheckBoxIds[i])
                                subordinate.visibility = View.GONE
                                if (subordinate.isChecked) {
                                    toggleDisablingListener(subordinate)
                                }
                            }
                        }
                    }

                    if (!(file?.isFolder == true && isChecked && file?.isSharedWithMe == true) ||       // see (1)
                        isFederated
                    ) {
                        updatePermissionsToShare()
                    }
                }

                R.id.canEditCreateCheckBox -> {
                    Timber.v("canEditCreateCheckBox toggled to $isChecked")
                    syncCanEditSwitch(compound, isChecked)
                    updatePermissionsToShare()
                }

                R.id.canEditChangeCheckBox -> {
                    Timber.v("canEditChangeCheckBox toggled to $isChecked")
                    syncCanEditSwitch(compound, isChecked)
                    updatePermissionsToShare()
                }

                R.id.canEditDeleteCheckBox -> {
                    Timber.v("canEditDeleteCheckBox toggled to $isChecked")
                    syncCanEditSwitch(compound, isChecked)
                    updatePermissionsToShare()
                }
            } // updatePermissionsToShare()   // see (1)





        }


        private fun syncCanEditSwitch(subordinateCheckBoxView: View, isChecked: Boolean) {
            val canEditCompound = binding.canEditSwitch
            if (isChecked) {
                if (!canEditCompound.isChecked) {
                    toggleDisablingListener(canEditCompound)
                }
            } else {
                var allDisabled = true
                run {
                    var i = 0
                    while (allDisabled && i < sSubordinateCheckBoxIds.size) {
                        allDisabled =
                            allDisabled and (sSubordinateCheckBoxIds[i] == subordinateCheckBoxView.id || !(view?.findViewById<View>(
                                sSubordinateCheckBoxIds[i]
                            ) as CheckBox).isChecked)
                        i++
                    }
                }
                if (canEditCompound.isChecked && allDisabled) {
                    toggleDisablingListener(canEditCompound)
                    for (i in sSubordinateCheckBoxIds.indices) {
                        view?.findViewById<View>(sSubordinateCheckBoxIds[i])?.visibility = View.GONE
                    }
                }
            }
        }


        private fun toggleDisablingListener(compound: CompoundButton) {
            compound.setOnCheckedChangeListener(null)
            compound.toggle()
            compound.setOnCheckedChangeListener(this)
        }
    }

    private fun observePrivateShareToEdit() {
        shareViewModel.privateShare.observe(
            this,
            EventObserver { uiResult ->
                when (uiResult) {
                    is UIResult.Success -> {
                        updateShare(uiResult.data)
                    }
                    is UIResult.Error -> {}
                    is UIResult.Loading -> {}
                }
            }
        )
    }

    private fun observePrivateShareEdition() {
        shareViewModel.privateShareEditionStatus.observe(
            this,
            EventObserver { uiResult ->
                when (uiResult) {
                    is UIResult.Error -> {
                        showError(getString(R.string.update_link_file_error), uiResult.error)
                        listener?.dismissLoading()
                    }
                    is UIResult.Loading -> {
                        listener?.showLoading()
                    }
                    is UIResult.Success -> {}
                }
            }
        )
    }


    private fun updatePermissionsToShare() {
        binding.privateShareErrorMessage.isVisible = false

        val sharePermissionsBuilder = SharePermissionsBuilder().apply {
            setSharePermission(binding.canShareSwitch.isChecked)
            if (file?.isFolder == true) {
                setUpdatePermission(binding.canEditChangeCheckBox.isChecked)
                setCreatePermission(binding.canEditCreateCheckBox.isChecked)
                setDeletePermission(binding.canEditDeleteCheckBox.isChecked)
            } else {
                setUpdatePermission(binding.canEditSwitch.isChecked)
            }
        }
        val permissions = sharePermissionsBuilder.build()

        shareViewModel.updatePrivateShare(share?.remoteId!!, permissions, account?.name!!)
    }

    private fun refreshPrivateShare(remoteId: String) {
        shareViewModel.refreshPrivateShare(remoteId)
    }


    private fun updateShare(updatedShare: OCShare?) {
        share = updatedShare
        refreshUiFromState()
    }


    private fun showError(genericErrorMessage: String, throwable: Throwable?) {
        binding.privateShareErrorMessage.apply {
            text = throwable?.parseError(genericErrorMessage, resources)
            isVisible = true
        }
    }

    companion object {
                private const val ARG_SHARE = "SHARE"
        private const val ARG_FILE = "FILE"
        private const val ARG_ACCOUNT = "ACCOUNT"

                private val sSubordinateCheckBoxIds =
            intArrayOf(R.id.canEditCreateCheckBox, R.id.canEditChangeCheckBox, R.id.canEditDeleteCheckBox)


        fun newInstance(shareToEdit: OCShare, sharedFile: OCFile, account: Account): EditPrivateShareFragment {
            val args = Bundle().apply {
                putParcelable(ARG_SHARE, shareToEdit)
                putParcelable(ARG_FILE, sharedFile)
                putParcelable(ARG_ACCOUNT, account)
            }

            return EditPrivateShareFragment().apply { arguments = args }
        }
    }
}
