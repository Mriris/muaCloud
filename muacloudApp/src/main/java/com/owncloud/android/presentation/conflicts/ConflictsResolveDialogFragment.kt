

package com.owncloud.android.presentation.conflicts

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.owncloud.android.R
import com.owncloud.android.extensions.avoidScreenshotsIfNeeded

class ConflictsResolveDialogFragment : DialogFragment() {

    private lateinit var listener: OnConflictDecisionMadeListener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = AlertDialog.Builder(requireActivity())
            .setIcon(R.drawable.ic_warning)
            .setTitle(R.string.conflict_title)
            .setMessage(R.string.conflict_message)
            .setPositiveButton(R.string.conflict_use_local_version) { _, _ ->
                listener.conflictDecisionMade(Decision.KEEP_LOCAL)
            }
            .setNeutralButton(R.string.conflict_keep_both) { _, _ ->
                listener.conflictDecisionMade(Decision.KEEP_BOTH)
            }
            .setNegativeButton(R.string.conflict_use_server_version) { _, _ ->
                listener.conflictDecisionMade(Decision.KEEP_SERVER)
            }
            .create()

        dialog.avoidScreenshotsIfNeeded()

        return dialog
    }

    override fun onCancel(dialog: DialogInterface) {
        listener.conflictDecisionMade(Decision.CANCEL)
    }

    fun showDialog(activity: AppCompatActivity) {
        val previousFragment = activity.supportFragmentManager.findFragmentByTag("dialog")
        val fragmentTransaction = activity.supportFragmentManager.beginTransaction()
        if (previousFragment != null) {
            fragmentTransaction.remove(previousFragment)
        }
        fragmentTransaction.addToBackStack(null)

        this.show(fragmentTransaction, "dialog")
    }

    interface OnConflictDecisionMadeListener {
        fun conflictDecisionMade(decision: Decision)
    }

    enum class Decision {
        CANCEL,
        KEEP_BOTH,
        KEEP_LOCAL,
        KEEP_SERVER
    }

    companion object {
        fun newInstance(onConflictDecisionMadeListener: OnConflictDecisionMadeListener): ConflictsResolveDialogFragment {
            return ConflictsResolveDialogFragment().apply {
                listener = onConflictDecisionMadeListener
            }
        }
    }
}
