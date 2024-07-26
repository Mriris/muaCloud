

package com.owncloud.android.ui.preview

import android.accounts.Account
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.work.WorkInfo
import com.owncloud.android.R
import com.owncloud.android.domain.files.model.OCFile
import com.owncloud.android.extensions.observeWorkerTillItFinishes
import com.owncloud.android.presentation.transfers.TransfersViewModel
import com.owncloud.android.ui.fragment.FileFragment
import com.owncloud.android.usecases.transfers.downloads.GetLiveDataForDownloadingFileUseCase
import com.owncloud.android.utils.PreferenceUtils
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class FileDownloadFragment : FileFragment() {
    private var account: Account? = null
    private var ignoreFirstSavedState = false
    private var error = false
    private var progressBar: ProgressBar? = null
    private var liveData: LiveData<WorkInfo?>? = null

    private val transfersViewModel: TransfersViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        with(requireArguments()) {
            file = getParcelable<Parcelable>(ARG_FILE) as OCFile?
            ignoreFirstSavedState = getBoolean(ARG_IGNORE_FIRST)
            account = getParcelable(ARG_ACCOUNT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        return inflater.inflate(R.layout.file_download_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        savedInstanceState?.let {
            if (!ignoreFirstSavedState) {
                file = it.getParcelable<Parcelable>(EXTRA_FILE) as OCFile?
                account = it.getParcelable(EXTRA_ACCOUNT)
                error = it.getBoolean(EXTRA_ERROR)
            } else {
                ignoreFirstSavedState = false
            }
        }

        view.filterTouchesWhenObscured = PreferenceUtils.shouldDisallowTouchesWithOtherVisibleWindows(context)

        progressBar = view.findViewById(R.id.progressBar)

        view.findViewById<View>(R.id.cancelBtn).setOnClickListener {
            transfersViewModel.cancelTransfersForFile(file)
            requireActivity().finish()
        }

        if (error) {
            setButtonsForRemote(view)
        } else {
            setButtonsForTransferring(view)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.apply {
            putParcelable(EXTRA_FILE, file)
            putParcelable(EXTRA_ACCOUNT, account)
            putBoolean(EXTRA_ERROR, error)
        }
    }

    override fun onStart() {
        super.onStart()
        listenForTransferProgress()
    }

    override fun onStop() {
        leaveTransferProgress()
        super.onStop()
    }


    private fun setButtonsForTransferring(rootView: View?) {
        rootView?.run {
            findViewById<View>(R.id.cancelBtn).isVisible = true
            findViewById<View>(R.id.progressBar).isVisible = true
            findViewById<TextView>(R.id.progressText).apply {
                setText(R.string.downloader_download_in_progress_ticker)
                isVisible = true
            }

            findViewById<View>(R.id.errorText).isVisible = false
            findViewById<View>(R.id.error_image).isVisible = false
        }
    }


    private fun setButtonsForDown(rootView: View?) {
        rootView?.run {
            findViewById<View>(R.id.cancelBtn).isVisible = false
            findViewById<View>(R.id.progressBar).isVisible = false

            findViewById<TextView>(R.id.progressText).apply {
                setText(R.string.common_loading)
                isVisible = true
            }

            findViewById<View>(R.id.errorText).isVisible = false
            findViewById<View>(R.id.error_image).isVisible = false
        }
    }


    private fun setButtonsForRemote(rootView: View?) {
        rootView?.run {
            findViewById<View>(R.id.cancelBtn).isVisible = false

            findViewById<View>(R.id.progressBar).isVisible = false
            findViewById<View>(R.id.progressText).isVisible = false

            findViewById<View>(R.id.errorText).isVisible = true
            findViewById<View>(R.id.error_image).isVisible = true
        }
    }

    override fun onFileMetadataChanged(updatedFile: OCFile?) {
        updatedFile?.let { file = it }
    }

    override fun onFileMetadataChanged() {
        mContainerActivity.storageManager?.let {
            file = it.getFileByPath(file.remotePath)
        }
    }

    override fun onFileContentChanged() {}

    override fun updateViewForSyncInProgress() {
        setButtonsForTransferring(view)
    }

    override fun updateViewForSyncOff() {
        if (file.isAvailableLocally) {
            setButtonsForDown(view)
        } else {
            setButtonsForRemote(view)
        }
    }

    private fun listenForTransferProgress() {
        val getLiveDataForDownloadingFileUseCase: GetLiveDataForDownloadingFileUseCase by inject()
        account?.let {
            liveData =
                getLiveDataForDownloadingFileUseCase(GetLiveDataForDownloadingFileUseCase.Params(it.name, file))
            liveData?.observeWorkerTillItFinishes(
                owner = this,
                onWorkEnqueued = { progressBar?.isIndeterminate = true },
                onWorkRunning = { workProgress ->
                    progressBar?.apply {
                        if (workProgress == -1) {
                            isIndeterminate = true
                        } else {
                            isIndeterminate = false
                            progress = workProgress
                            invalidate()
                        }
                    }
                },
                onWorkSucceeded = { },
                onWorkFailed = { },
                removeObserverAfterNull = false,
            )
        }
        setButtonsForTransferring(view)
    }

    private fun leaveTransferProgress() {
        liveData?.removeObservers(this)
    }

    fun setError(error: Boolean) {
        this.error = error
    }

    companion object {
        const val EXTRA_FILE = "FILE"
        const val EXTRA_ACCOUNT = "ACCOUNT"
        private const val EXTRA_ERROR = "ERROR"
        private const val ARG_FILE = "FILE"
        private const val ARG_IGNORE_FIRST = "IGNORE_FIRST"
        private const val ARG_ACCOUNT = "ACCOUNT"


        fun newInstance(file: OCFile?, account: Account, ignoreFirstSavedState: Boolean): Fragment {
            val args = Bundle().apply {
                putParcelable(ARG_FILE, file)
                putParcelable(ARG_ACCOUNT, account)
                putBoolean(ARG_IGNORE_FIRST, ignoreFirstSavedState)
            }

            return FileDownloadFragment().apply { arguments = args }
        }
    }
}
