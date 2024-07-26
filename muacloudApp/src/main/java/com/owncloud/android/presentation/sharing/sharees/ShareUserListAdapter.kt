
package com.owncloud.android.presentation.sharing.sharees

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.owncloud.android.R
import com.owncloud.android.domain.sharing.shares.model.OCShare
import com.owncloud.android.domain.sharing.shares.model.ShareType
import com.owncloud.android.utils.PreferenceUtils


class ShareUserListAdapter(
    private val mContext: Context, resource: Int,
    private var shares: List<OCShare>,
    private val listener: ShareUserAdapterListener
) : ArrayAdapter<OCShare>(mContext, resource) {

    init {
        shares = shares.sortedBy { it.sharedWithDisplayName }
    }

    override fun getCount(): Int = shares.size

    override fun getItem(position: Int): OCShare = shares[position]

    override fun getItemId(position: Int): Long = 0

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflator = mContext
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflator.inflate(R.layout.share_user_item, parent, false)

        view.filterTouchesWhenObscured = PreferenceUtils.shouldDisallowTouchesWithOtherVisibleWindows(mContext)

        if (shares.size > position) {
            val share = shares[position]

            val userName = view.findViewById<TextView>(R.id.userOrGroupName)
            val iconView = view.findViewById<ImageView>(R.id.icon)
            var name = share.sharedWithDisplayName
            name = if (share.sharedWithAdditionalInfo!!.isEmpty())
                name
            else
                name + " (" + share.sharedWithAdditionalInfo + ")"
            var icon = context.resources.getDrawable(R.drawable.ic_user, null)
            iconView.tag = R.drawable.ic_user
            if (share.shareType == ShareType.GROUP) {
                name = context.getString(R.string.share_group_clarification, name)
                icon = context.resources.getDrawable(R.drawable.ic_group, null)
                iconView.tag = R.drawable.ic_group
            }
            userName.text = name
            iconView.setImageDrawable(icon)

            val editShareButton = view.findViewById<ImageView>(R.id.editShareButton)
            editShareButton.setOnClickListener { listener.editShare(shares[position]) }

            val unshareButton = view.findViewById<ImageView>(R.id.unshareButton)
            unshareButton.setOnClickListener { listener.unshareButtonPressed(shares[position]) }

        }
        return view
    }

    interface ShareUserAdapterListener {
        fun unshareButtonPressed(share: OCShare)
        fun editShare(share: OCShare)
    }
}
