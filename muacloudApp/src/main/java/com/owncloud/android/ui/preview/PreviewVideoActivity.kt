

package com.owncloud.android.ui.preview

import android.accounts.Account
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.Window
import androidx.annotation.OptIn
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.AudioAttributes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlaybackException
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.trackselection.AdaptiveTrackSelection
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.PlayerView
import com.owncloud.android.R
import com.owncloud.android.databinding.VideoPreviewBinding
import com.owncloud.android.domain.files.model.OCFile
import com.owncloud.android.domain.utils.Event
import com.owncloud.android.extensions.filterMenuOptions
import com.owncloud.android.extensions.sendDownloadedFilesByShareSheet
import com.owncloud.android.extensions.showErrorInSnackbar
import com.owncloud.android.presentation.authentication.AccountUtils
import com.owncloud.android.presentation.common.UIResult
import com.owncloud.android.presentation.files.operations.FileOperation.SetFilesAsAvailableOffline
import com.owncloud.android.presentation.files.operations.FileOperation.UnsetFilesAsAvailableOffline
import com.owncloud.android.presentation.files.operations.FileOperationsViewModel
import com.owncloud.android.presentation.files.removefile.RemoveFilesDialogFragment.Companion.TAG_REMOVE_FILES_DIALOG_FRAGMENT
import com.owncloud.android.presentation.files.removefile.RemoveFilesDialogFragment.Companion.newInstance
import com.owncloud.android.presentation.previews.PreviewVideoViewModel
import com.owncloud.android.presentation.transfers.TransfersViewModel
import com.owncloud.android.ui.activity.FileActivity
import com.owncloud.android.ui.activity.FileDisplayActivity
import com.owncloud.android.ui.fragment.FileFragment
import com.owncloud.android.ui.preview.PrepareVideoPlayerAsyncTask.OnPrepareVideoPlayerTaskListener
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

@OptIn(UnstableApi::class)
class PreviewVideoActivity : FileActivity(), Player.Listener, OnPrepareVideoPlayerTaskListener, FileFragment.ContainerActivity, MenuProvider {
    private var account: Account? = null

    private lateinit var playerView: PlayerView
    private var player: ExoPlayer? = null
    private lateinit var trackSelector: DefaultTrackSelector
    private var playWhenReady = true
    private var playbackPosition: Long = 0
    private var windowInsetsController: WindowInsetsControllerCompat? = null
    private val previewVideoViewModel: PreviewVideoViewModel by viewModel()
    private val fileOperationsViewModel: FileOperationsViewModel by viewModel()
    private val transfersViewModel: TransfersViewModel by viewModel()

    private lateinit var binding: VideoPreviewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY)
        super.onCreate(savedInstanceState)
        binding = VideoPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        addMenuProvider(this)


        if (savedInstanceState == null) {
            val launchingIntent = intent
            if (Build.VERSION.SDK_INT >= 33) { // getParcelableExtra(String!): T? starting from Android Build.VERSION_CODES.TIRAMISU.
                account = launchingIntent.getParcelableExtra(EXTRA_ACCOUNT, Account::class.java)
                file = launchingIntent.getParcelableExtra(EXTRA_FILE, OCFile::class.java)
            } else {
                account = launchingIntent.getParcelableExtra(EXTRA_ACCOUNT)
                file = launchingIntent.getParcelableExtra(EXTRA_FILE)
            }
            playbackPosition = launchingIntent.getLongExtra(EXTRA_PLAY_POSITION, 0)
        } else {
            if (Build.VERSION.SDK_INT >= 33) { // getParcelableExtra(String!): T? starting from Android Build.VERSION_CODES.TIRAMISU.
                account = savedInstanceState.getParcelable(EXTRA_ACCOUNT, Account::class.java)
                file = savedInstanceState.getParcelable(EXTRA_FILE, OCFile::class.java)
            } else {
                account = savedInstanceState.getParcelable(EXTRA_ACCOUNT)
                file = savedInstanceState.getParcelable(EXTRA_FILE)
            }
            playWhenReady = savedInstanceState.getBoolean(EXTRA_AUTOPLAY, true)
            playbackPosition = savedInstanceState.getLong(EXTRA_PLAY_POSITION, 0)
        }

        checkNotNull(file) { "Instanced with a NULL OCFile" }
        checkNotNull(account) { "Instanced with a NULL ownCloud Account" }
        check(file.isVideo) { "Not a video file" }

        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
            title = file.fileName
        }
        playerView = binding.videoPlayer
        playerView.setShowPreviousButton(false)
        playerView.setShowNextButton(false)
        initWindowInsetsController()

        playerView.setFullscreenButtonClickListener { isFullScreen ->
            if (isFullScreen) {

                enterImmersiveMode()
            } else {

                exitImmersiveMode()
            }
        }
        playerView.setControllerVisibilityListener(PlayerView.ControllerVisibilityListener { visibility -> setActionBarVisibility(visibility) })
        startObservingFileOperations()
    }

    override fun onStart() {
        super.onStart()
        initializePlayer()
        isOpen = true
        currentFilePreviewing = file
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
        isOpen = false
        currentFilePreviewing = null
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
    }

    private fun initializePlayer() {
        val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory()
        trackSelector = DefaultTrackSelector(this, videoTrackSelectionFactory)
        player = ExoPlayer.Builder(this)
            .setAudioAttributes(AudioAttributes.DEFAULT, true)
            .setHandleAudioBecomingNoisy(true)
            .setTrackSelector(trackSelector)
            .setLoadControl(DefaultLoadControl.Builder().build()).build()

        player?.addListener(this)
        playerView.player = player

        PrepareVideoPlayerAsyncTask(this, this, file, account).execute()
    }

    private fun releasePlayer() {
        player?.let { exoPlayer ->
            playWhenReady = exoPlayer.playWhenReady
            playbackPosition = exoPlayer.currentPosition
            exoPlayer.removeListener(this)
            exoPlayer.release()
        }
        player = null
    }

    override fun onPlayerError(error: PlaybackException) {
        Timber.e(error, "Error in video player")

        showAlertDialog(PreviewVideoErrorAdapter.handlePreviewVideoError(error as ExoPlaybackException, this))
    }

    override fun OnPrepareVideoPlayerTaskCallback(mediaSource: MediaSource?) {
        if (mediaSource != null) {
            player?.let { exoPlayer ->
                exoPlayer.addMediaSource(mediaSource)
                exoPlayer.playWhenReady = playWhenReady
                exoPlayer.seekTo(playbackPosition)
                exoPlayer.prepare()
            }
        }

    }


    private fun showAlertDialog(previewVideoError: PreviewVideoError) {
        AlertDialog.Builder(this)
            .setMessage(previewVideoError.errorMessage)
            .setPositiveButton(
                android.R.string.VideoView_error_button
            ) { _: DialogInterface?, _: Int ->
                if (previewVideoError.isFileSyncNeeded) {

                    fileOperationsHelper.syncFile(file)
                }
            }
            .setCancelable(false)
            .show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putParcelable(EXTRA_FILE, file)
        outState.putParcelable(EXTRA_ACCOUNT, account)
        if (player != null) {
            outState.putBoolean(EXTRA_AUTOPLAY, playWhenReady)
            outState.putLong(EXTRA_PLAY_POSITION, player?.currentPosition ?: 0L)
        }
    }

    private fun initWindowInsetsController() {
        windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController!!.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    private fun enterImmersiveMode() {
        windowInsetsController!!.hide(WindowInsetsCompat.Type.systemBars())
    }

    private fun exitImmersiveMode() {
        windowInsetsController!!.show(WindowInsetsCompat.Type.systemBars())
    }

    private fun setActionBarVisibility(visibility: Int) {
        if (visibility == View.VISIBLE) {
            supportActionBar?.show()
        } else if (visibility == View.GONE) {
            supportActionBar?.hide()
        }
    }

    private fun seeDetails() {
        releasePlayer()
        showDetails(file)
    }

    private fun openFile() {
        player?.pause()
        fileOperationsHelper.openFile(file)
    }

    override fun showDetails(file: OCFile) {
        val showDetailsIntent = Intent(this, FileDisplayActivity::class.java).apply {
            action = FileDisplayActivity.ACTION_DETAILS
            putExtra(FileActivity.EXTRA_FILE, file)
            putExtra(FileActivity.EXTRA_ACCOUNT, AccountUtils.getCurrentOwnCloudAccount(this@PreviewVideoActivity))
        }
        finishAffinity()
        startActivity(showDetailsIntent)
    }

    private fun <T> collectLatestLifecycleFlow(
        flow: Flow<T>,
        lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
        collect: suspend (T) -> Unit
    ) {
        lifecycleScope.launch {
            repeatOnLifecycle(lifecycleState) {
                flow.collectLatest(collect)
            }
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.file_actions_menu, menu)
    }

    override fun onPrepareMenu(menu: Menu) {
        val safeFile = file
        val accountName = account!!.name
        previewVideoViewModel.filterMenuOptions(safeFile, accountName)

        collectLatestLifecycleFlow(
            previewVideoViewModel.menuOptions
        ) { menuOptions ->
            val hasWritePermission: Boolean = safeFile.hasWritePermission
            menu.filterMenuOptions(menuOptions, hasWritePermission)
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.action_share_file -> {
                fileOperationsHelper.showShareFile(file)
                true
            }

            R.id.action_open_file_with -> {
                openFile()
                true
            }

            R.id.action_remove_file -> {
                player?.pause()
                val dialog = newInstance(file)
                dialog.show(supportFragmentManager, TAG_REMOVE_FILES_DIALOG_FRAGMENT)
                true
            }

            R.id.action_see_details -> {
                seeDetails()
                true
            }

            R.id.action_send_file -> {
                player?.pause()
                sendDownloadedFilesByShareSheet(listOf<OCFile>(file))
                true
            }

            R.id.action_sync_file -> {
                fileOperationsHelper.syncFile(file)
                true
            }

            R.id.action_set_available_offline -> {
                val fileToSetAsAvailableOffline = ArrayList<OCFile>()
                fileToSetAsAvailableOffline.add(file)
                fileOperationsViewModel.performOperation(SetFilesAsAvailableOffline(fileToSetAsAvailableOffline))
                true
            }

            R.id.action_unset_available_offline -> {
                val fileToUnsetAsAvailableOffline = ArrayList<OCFile>()
                fileToUnsetAsAvailableOffline.add(file)
                fileOperationsViewModel.performOperation(UnsetFilesAsAvailableOffline(fileToUnsetAsAvailableOffline))
                true
            }

            R.id.action_download_file -> {
                fileOperationsHelper.syncFile(file)
                true
            }

            R.id.action_cancel_sync -> {
                val fileList = ArrayList<OCFile>()
                fileList.add(file)
                transfersViewModel.cancelTransfersRecursively(fileList, account?.name.orEmpty())
                true
            }

            android.R.id.home -> {
                finish()
                true
            }

            else -> {
                false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return false
    }

    companion object {
        const val EXTRA_FILE = "FILE"
        const val EXTRA_ACCOUNT = "ACCOUNT"
        var isOpen: Boolean = false
        var currentFilePreviewing: OCFile? = null


        const val EXTRA_AUTOPLAY = "AUTOPLAY"


        const val EXTRA_PLAY_POSITION = "START_POSITION"


        fun canBePreviewed(file: OCFile?): Boolean {
            return file != null && file.isVideo
        }
    }
}
