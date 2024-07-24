

package com.owncloud.android.presentation.sharing



import android.accounts.Account
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.owncloud.android.R
import com.owncloud.android.domain.sharing.shares.model.OCShare
import com.owncloud.android.domain.sharing.shares.model.ShareType
import com.owncloud.android.extensions.avoidScreenshotsIfNeeded
import com.owncloud.android.ui.dialog.ConfirmationDialogFragment
import com.owncloud.android.ui.dialog.ConfirmationDialogFragment.ConfirmationDialogFragmentListener
import timber.log.Timber

class RemoveShareDialogFragment : ConfirmationDialogFragment(), ConfirmationDialogFragmentListener {
    private var targetShare: OCShare? = null
    private var account: Account? = null


    private var listener: ShareFragmentListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        arguments?.let {
            targetShare = it.getParcelable(ARG_TARGET_SHARE)
            account = it.getParcelable(ARG_ACCOUNT)
        }

        setOnConfirmationListener(this)

        dialog.avoidScreenshotsIfNeeded()

        return dialog
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = activity as ShareFragmentListener?
        } catch (e: IllegalStateException) {
            throw IllegalStateException(requireActivity().toString() + " must implement OnShareFragmentInteractionListener")
        }
    }


    override fun onConfirmation(callerTag: String) {
        Timber.d("Removing share ${targetShare?.name}")
        listener?.deleteShare(targetShare?.remoteId!!)
    }

    override fun onCancel(callerTag: String) {
        // nothing to do here
    }

    override fun onNeutral(callerTag: String) {
        // nothing to do here
    }

    companion object {

        private const val ARG_TARGET_SHARE = "TARGET_SHARE"
        private const val ARG_ACCOUNT = "ACCOUNT"


        fun newInstance(share: OCShare, account: Account): RemoveShareDialogFragment {
            val frag = RemoveShareDialogFragment()
            val args = Bundle()

            args.putInt(
                ARG_MESSAGE_RESOURCE_ID,
                R.string.confirmation_remove_share_message
            )

            val privateShareName = if (share.sharedWithAdditionalInfo!!.isEmpty()) {
                share.sharedWithDisplayName
            } else {
                share.sharedWithDisplayName + " (" + share.sharedWithAdditionalInfo + ")"
            }

            args.putStringArray(
                ARG_MESSAGE_ARGUMENTS,
                arrayOf(if (share.name!!.isNotEmpty()) share.name!! else privateShareName)
            )
            args.putInt(
                ARG_TITLE_ID,
                if (share.shareType == ShareType.PUBLIC_LINK)
                    R.string.confirmation_remove_public_share_title else
                    R.string.confirmation_remove_private_share_title
            )
            args.putInt(ARG_POSITIVE_BTN_RES, R.string.common_yes)
            args.putInt(ARG_NEUTRAL_BTN_RES, R.string.common_no)
            args.putInt(ARG_NEGATIVE_BTN_RES, -1)
            args.putParcelable(ARG_TARGET_SHARE, share)
            args.putParcelable(ARG_ACCOUNT, account)
            frag.arguments = args

            return frag
        }
    }
}
