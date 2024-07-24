

package com.owncloud.android.presentation.shares

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.owncloud.android.R
import com.owncloud.android.databinding.SharesFragmentBinding

class SharesFragment : Fragment() {
    private var _binding: SharesFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SharesFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initViews()
    }

    private fun initViews() {
        showOrHideEmptyView()
    }

    private fun showOrHideEmptyView() {
        with(binding.emptyDataParent) {
            root.isVisible = true
            listEmptyDatasetIcon.setImageResource(R.drawable.ic_ocis_shares)
            listEmptyDatasetTitle.setText(R.string.shares_list_empty_title)
            listEmptyDatasetSubTitle.setText(R.string.shares_list_empty_subtitle)
        }
    }
}
