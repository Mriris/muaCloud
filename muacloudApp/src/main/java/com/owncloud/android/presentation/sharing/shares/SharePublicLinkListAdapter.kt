package com.owncloud.android.presentation.sharing.shares



import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.owncloud.android.databinding.SharePublicLinkItemBinding
import com.owncloud.android.domain.sharing.shares.model.OCShare
import com.owncloud.android.utils.PreferenceUtils


class SharePublicLinkListAdapter(
    private val mContext: Context,
    resource: Int,
    private var publicLinks: List<OCShare>,
    private val listener: SharePublicLinkAdapterListener
) : ArrayAdapter<OCShare>(mContext, resource) {

    private lateinit var binding: SharePublicLinkItemBinding

    init {
        publicLinks = publicLinks.sortedBy { it.name }
    }

    override fun getCount(): Int = publicLinks.size

    override fun getItem(position: Int): OCShare = publicLinks[position]

    override fun getItemId(position: Int): Long = 0

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflator = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = SharePublicLinkItemBinding.inflate(inflator).apply {
            root.filterTouchesWhenObscured = PreferenceUtils.shouldDisallowTouchesWithOtherVisibleWindows(mContext)
        }

        if (publicLinks.size > position) {
            val share = publicLinks[position]

            binding.publicLinkName.text = if (share.name.isNullOrEmpty()) share.token else share.name

            binding.getPublicLinkButton.setOnClickListener { listener.copyOrSendPublicLink(publicLinks[position]) }

            binding.deletePublicLinkButton.setOnClickListener { listener.removeShare(publicLinks[position]) }

            binding.editPublicLinkButton.setOnClickListener { listener.editPublicShare(publicLinks[position]) }
        }

        return binding.root
    }

    interface SharePublicLinkAdapterListener {
        fun copyOrSendPublicLink(share: OCShare)

        fun removeShare(share: OCShare)

        fun editPublicShare(share: OCShare)
    }
}
