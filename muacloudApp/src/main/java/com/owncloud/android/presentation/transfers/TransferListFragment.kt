

package com.owncloud.android.presentation.transfers

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.owncloud.android.R
import com.owncloud.android.databinding.FragmentTransferListBinding
import com.owncloud.android.domain.spaces.model.OCSpace
import com.owncloud.android.domain.transfers.model.OCTransfer
import com.owncloud.android.domain.transfers.model.TransferResult
import com.owncloud.android.extensions.collectLatestLifecycleFlow
import com.owncloud.android.presentation.authentication.AccountUtils
import com.owncloud.android.ui.activity.FileActivity
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class TransferListFragment : Fragment() {

    private val transfersViewModel by viewModel<TransfersViewModel>()

    private var _binding: FragmentTransferListBinding? = null
    val binding get() = _binding!!

    private lateinit var transfersAdapter: TransfersAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTransferListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        transfersAdapter = TransfersAdapter(
            cancel = { transfer ->
                transfersViewModel.cancelUpload(transfer)
            },
            retry = { transfer: OCTransfer ->
                if (transfer.lastResult == TransferResult.CREDENTIAL_ERROR) {
                    val parentActivity = requireActivity() as FileActivity
                    val account = AccountUtils.getOwnCloudAccountByName(requireContext(), transfer.accountName)
                    parentActivity.fileOperationsHelper.checkCurrentCredentials(account)
                } else {
                    val file = File(transfer.localPath)
                    if (file.exists()) {
                        transfersViewModel.retryUploadFromSystem(transfer.id!!)
                    } else if (DocumentFile.isDocumentUri(requireContext(), Uri.parse(transfer.localPath))) {
                        transfersViewModel.retryUploadFromContentUri(transfer.id!!)
                    } else {
                        Snackbar.make(
                            view,
                            getString(R.string.local_file_not_found_toast),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            },
            clearFailed = {
                transfersViewModel.clearFailedTransfers()
            },
            retryFailed = {
                transfersViewModel.retryFailedTransfers()
            },
            clearSuccessful = {
                transfersViewModel.clearSuccessfulTransfers()
            },
        )
        binding.transfersRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            itemAnimator = DefaultItemAnimator()
            adapter = transfersAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }

        collectLatestLifecycleFlow(transfersViewModel.transfersWithSpaceStateFlow) { transfers ->
            val recyclerViewState = binding.transfersRecyclerView.layoutManager?.onSaveInstanceState()
            setData(transfers)
            binding.transfersRecyclerView.layoutManager?.onRestoreInstanceState(recyclerViewState)
        }

        transfersViewModel.workInfosListLiveData.observe(viewLifecycleOwner) { workInfos ->
            workInfos.forEach { workInfo ->
                transfersAdapter.updateTransferProgress(workInfo)
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun setData(transfersWithSpace: List<Pair<OCTransfer, OCSpace?>>) {
        binding.transfersRecyclerView.isVisible = transfersWithSpace.isNotEmpty()
        binding.transfersListEmpty.apply {
            root.isVisible = transfersWithSpace.isEmpty()
            listEmptyDatasetIcon.setImageResource(R.drawable.ic_uploads)
            listEmptyDatasetTitle.setText(R.string.upload_list_empty)
            listEmptyDatasetSubTitle.setText(R.string.upload_list_empty_subtitle)
        }
        transfersAdapter.setData(transfersWithSpace)
    }
}
