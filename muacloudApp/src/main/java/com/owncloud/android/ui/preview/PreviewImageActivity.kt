
package com.owncloud.android.ui.preview

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import androidx.work.WorkInfo
import com.owncloud.android.R
import com.owncloud.android.data.providers.SharedPreferencesProvider
import com.owncloud.android.domain.exceptions.AccountNotFoundException
import com.owncloud.android.domain.files.model.FileListOption
import com.owncloud.android.domain.files.model.OCFile
import com.owncloud.android.domain.files.model.OCFile.Companion.ROOT_PARENT_ID
import com.owncloud.android.domain.files.usecases.SortFilesUseCase
import com.owncloud.android.domain.utils.Event
import com.owncloud.android.extensions.showErrorInSnackbar
import com.owncloud.android.presentation.authentication.AccountUtils
import com.owncloud.android.presentation.common.UIResult
import com.owncloud.android.presentation.files.SortOrder
import com.owncloud.android.presentation.files.SortType
import com.owncloud.android.presentation.files.operations.FileOperation
import com.owncloud.android.presentation.files.operations.FileOperationsViewModel
import com.owncloud.android.ui.activity.FileActivity
import com.owncloud.android.ui.activity.FileDisplayActivity
import com.owncloud.android.ui.fragment.FileFragment
import com.owncloud.android.usecases.transfers.DOWNLOAD_ADDED_MESSAGE
import com.owncloud.android.usecases.transfers.DOWNLOAD_FINISH_MESSAGE
import com.owncloud.android.utils.PreferenceUtils
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber


class PreviewImageActivity : FileActivity(),
    FileFragment.ContainerActivity,
    OnPageChangeListener {

    private val previewImageViewModel: PreviewImageViewModel by viewModel()
    private val fileOperationsViewModel: FileOperationsViewModel by viewModel()

    private lateinit var viewPager: ViewPager
    private lateinit var previewImagePagerAdapter: PreviewImagePagerAdapter
    private var savedPosition = 0
    private var hasSavedPosition = false
    private var localBroadcastManager: LocalBroadcastManager? = null
    private var fullScreenAnchorView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.preview_image_activity)

        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
            setHomeActionContentDescription(R.string.common_back)
            setHomeButtonEnabled(true)
        }
        showActionBar(false)

        fullScreenAnchorView = window.decorView

        fullScreenAnchorView?.setOnSystemUiVisibilityChangeListener { flags ->
            val visible = flags and View.SYSTEM_UI_FLAG_HIDE_NAVIGATION == 0
            if (visible) {
                showActionBar(true)
                setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            } else {
                showActionBar(false)
                setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
        }
        window.statusBarColor = ContextCompat.getColor(this, R.color.owncloud_blue_dark_transparent)
        localBroadcastManager = LocalBroadcastManager.getInstance(this)
    }

    private fun startObservingFinishedDownloads() {
        previewImageViewModel.startListeningToDownloadsFromAccount(account = account)
        previewImageViewModel.downloads.observe(this) { pairFileWork ->
            if (pairFileWork.isEmpty()) return@observe

            pairFileWork.forEach { fileWork ->
                previewImagePagerAdapter.onDownloadEvent(
                    fileWork.first,
                    if (fileWork.second.state.isFinished) DOWNLOAD_FINISH_MESSAGE else DOWNLOAD_ADDED_MESSAGE,
                    fileWork.second.state == WorkInfo.State.SUCCEEDED
                )
            }
        }
    }

    private fun stopObservingWorkers() {
        previewImageViewModel.downloads.removeObservers(this)
    }

    private fun startObservingFileOperations() {
        fileOperationsViewModel.removeFileLiveData.observe(this, Event.EventObserver { uiResult ->
            when (uiResult) {
                is UIResult.Error -> {
                    dismissLoadingDialog()
                    showErrorInSnackbar(R.string.remove_fail_msg, uiResult.getThrowableOrNull())
                }

                is UIResult.Loading -> showLoadingDialog(R.string.wait_a_moment)
                is UIResult.Success -> {
                    dismissLoadingDialog()
                    finish()
                }
            }
        })

        fileOperationsViewModel.syncFileLiveData.observe(this, Event.EventObserver { uiResult ->
            if (uiResult is UIResult.Error && uiResult.error is AccountNotFoundException) {
                showRequestAccountChangeNotice(getString(R.string.sync_fail_ticker_unauthorized), false)
            }
        })
    }

    private fun initViewPager() {

        val parentPath = file.remotePath.substring(
            0,
            file.remotePath.lastIndexOf(file.fileName)
        )
        var parentFolder = storageManager.getFileByPath(parentPath, file.spaceId)
        if (parentFolder == null) {

            parentFolder = storageManager.getFileByPath(OCFile.ROOT_PATH, file.spaceId)
        }

        val sharedPreferencesProvider: SharedPreferencesProvider by inject()
        val sortType = sharedPreferencesProvider.getInt(SortType.PREF_FILE_LIST_SORT_TYPE, SortType.SORT_TYPE_BY_NAME.ordinal)
        val sortOrder = sharedPreferencesProvider.getInt(SortOrder.PREF_FILE_LIST_SORT_ORDER, SortOrder.SORT_ORDER_ASCENDING.ordinal)
        val sortFilesUseCase: SortFilesUseCase by inject()
        val imageFiles = sortFilesUseCase(
            SortFilesUseCase.Params(
                listOfFiles = storageManager.getFolderImages(parentFolder),
                sortType = com.owncloud.android.domain.files.usecases.SortType.fromPreferences(sortType),
                ascending = SortOrder.fromPreference(sortOrder) == SortOrder.SORT_ORDER_ASCENDING
            )
        )
        previewImagePagerAdapter = PreviewImagePagerAdapter(
            supportFragmentManager,
            account,
            imageFiles.toMutableList()
        )

        viewPager = findViewById(R.id.fragmentPager)
        viewPager.apply {
            filterTouchesWhenObscured = PreferenceUtils.shouldDisallowTouchesWithOtherVisibleWindows(context)

            var position = if (hasSavedPosition) savedPosition else previewImagePagerAdapter.getFilePosition(file)
            position = if (position >= 0) position else 0
            adapter = previewImagePagerAdapter
            addOnPageChangeListener(this@PreviewImageActivity)
            currentItem = position
            if (position == 0) {








                viewPager.post { onPageSelected(viewPager.currentItem) }
            }
        }
        startObservingFinishedDownloads()
        startObservingFileOperations()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)



        delayedHide()
    }

    var mHideSystemUiHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            hideSystemUI(fullScreenAnchorView)
            showActionBar(false)
        }
    }

    private fun delayedHide(delayMillis: Int = INITIAL_HIDE_DELAY) {
        mHideSystemUiHandler.removeMessages(0)
        mHideSystemUiHandler.sendEmptyMessageDelayed(0, delayMillis.toLong())
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)


        if (!hasFocus) {
            mHideSystemUiHandler.removeMessages(0)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                if (isDrawerOpen()) {
                    closeDrawer()
                } else {
                    backToDisplayActivity()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        startObservingFinishedDownloads()
    }

    public override fun onPause() {
        stopObservingWorkers()
        super.onPause()
    }

    private fun backToDisplayActivity() {
        finish()
    }

    override fun showDetails(file: OCFile) {
        val showDetailsIntent = Intent(this, FileDisplayActivity::class.java).apply {
            action = FileDisplayActivity.ACTION_DETAILS
            putExtra(EXTRA_FILE, file)
            putExtra(EXTRA_ACCOUNT, AccountUtils.getCurrentOwnCloudAccount(this@PreviewImageActivity))
        }
        finishAffinity()
        startActivity(showDetailsIntent)
    }


    override fun onPageSelected(position: Int) {
        Timber.d("onPageSelected %s", position)
        if (operationsServiceBinder != null) {
            savedPosition = position
            hasSavedPosition = true
            val currentFile = previewImagePagerAdapter.getFileAt(position)
            updateActionBarTitle(currentFile.fileName)
            if (!previewImagePagerAdapter.pendingErrorAt(position)) {
                fileOperationsViewModel.performOperation(FileOperation.SynchronizeFileOperation(currentFile, account.name))
            }

            (viewPager.adapter as PreviewImagePagerAdapter?)?.resetZoom()
        } else {


            handler.post { onPageSelected(position) }
        }
    }


    override fun onPageScrollStateChanged(state: Int) {}


    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

    fun toggleFullScreen() {
        val safeFullScreenAnchorView = fullScreenAnchorView ?: return
        val visible = (safeFullScreenAnchorView.systemUiVisibility and View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0
        if (visible) {
            hideSystemUI(fullScreenAnchorView)
        } else {
            showSystemUI(fullScreenAnchorView)
        }
    }

    override fun onAccountSet(stateWasRecovered: Boolean) {
        super.onAccountSet(stateWasRecovered)
        account ?: return
        var file = file

        checkNotNull(file) { "Instanced with a NULL OCFile" }
        require(file.isImage) { "Non-image file passed as argument" }

        if (file.id!! > ROOT_PARENT_ID) {
            file = storageManager.getFileById(file.id!!)
        }
        if (file != null) {

            setFile(file) // reset after getting it fresh from storageManager
            updateActionBarTitle(getFile().fileName)
            initViewPager()
        } else {

            finish()
        }
    }

    private fun hideSystemUI(anchorView: View?) {
        anchorView?.systemUiVisibility =
            (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hides NAVIGATION BAR; Android >= 4.0
                    or View.SYSTEM_UI_FLAG_FULLSCREEN // hides STATUS BAR;     Android >= 4.1
                    or View.SYSTEM_UI_FLAG_IMMERSIVE // stays interactive;    Android >= 4.4
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE // draw full window;     Android >= 4.1
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN // draw full window;     Android >= 4.1
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
    }

    private fun showSystemUI(anchorView: View?) {
        anchorView?.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE // draw full window;     Android >= 4.1
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN // draw full window;     Android >= 4.1
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
    }

    override fun navigateToOption(fileListOption: FileListOption) {
        backToDisplayActivity()
        super.navigateToOption(fileListOption)
    }

    private fun showActionBar(show: Boolean) {
        val actionBar = supportActionBar ?: return
        if (show) {
            actionBar.show()
        } else {
            actionBar.hide()
        }
    }

    private fun updateActionBarTitle(title: String) {
        supportActionBar?.title = title
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return false
    }

    companion object {
        private const val INITIAL_HIDE_DELAY = 0 // immediate hide
    }
}
