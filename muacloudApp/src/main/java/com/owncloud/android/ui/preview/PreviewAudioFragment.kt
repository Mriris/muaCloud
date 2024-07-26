

package com.owncloud.android.ui.preview

import android.accounts.Account
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.owncloud.android.R
import com.owncloud.android.domain.files.model.OCFile
import com.owncloud.android.extensions.collectLatestLifecycleFlow
import com.owncloud.android.extensions.filterMenuOptions
import com.owncloud.android.extensions.sendDownloadedFilesByShareSheet
import com.owncloud.android.media.MediaControlView
import com.owncloud.android.media.MediaService
import com.owncloud.android.media.MediaServiceBinder
import com.owncloud.android.presentation.files.operations.FileOperation
import com.owncloud.android.presentation.files.operations.FileOperationsViewModel
import com.owncloud.android.presentation.files.removefile.RemoveFilesDialogFragment
import com.owncloud.android.presentation.files.removefile.RemoveFilesDialogFragment.Companion.TAG_REMOVE_FILES_DIALOG_FRAGMENT
import com.owncloud.android.presentation.previews.PreviewAudioViewModel
import com.owncloud.android.ui.fragment.FileFragment
import com.owncloud.android.utils.PreferenceUtils
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber


class PreviewAudioFragment : FileFragment() {
    private var account: Account? = null
    private var imagePreview: ImageView? = null
    private var savedPlaybackPosition = 0
    private var mediaServiceBinder: MediaServiceBinder? = null
    private var mediaController: MediaControlView? = null
    private var mediaServiceConnection: MediaServiceConnection? = null
    private var autoplay = true

    private val previewAudioViewModel by viewModel<PreviewAudioViewModel>()
    private val fileOperationsViewModel: FileOperationsViewModel by inject()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        return inflater.inflate(R.layout.preview_audio_fragment, container, false).apply {
            filterTouchesWhenObscured = PreferenceUtils.shouldDisallowTouchesWithOtherVisibleWindows(context)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imagePreview = view.findViewById(R.id.image_preview)
        mediaController = view.findViewById(R.id.media_controller)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Timber.v("onActivityCreated")
        val file: OCFile?
        val args = arguments
        if (savedInstanceState == null) {
            file = args!!.getParcelable(EXTRA_FILE)
            setFile(file)
            account = args.getParcelable(EXTRA_ACCOUNT)
            savedPlaybackPosition = args.getInt(EXTRA_PLAY_POSITION)
            autoplay = args.getBoolean(EXTRA_PLAYING)
        } else {
            file = savedInstanceState.getParcelable(EXTRA_FILE)
            setFile(file)
            account = savedInstanceState.getParcelable(EXTRA_ACCOUNT)
            savedPlaybackPosition = savedInstanceState.getInt(EXTRA_PLAY_POSITION, args!!.getInt(EXTRA_PLAY_POSITION))
            autoplay = savedInstanceState.getBoolean(EXTRA_PLAYING, args.getBoolean(EXTRA_PLAYING))
        }
        checkNotNull(file) { "Instanced with a NULL OCFile" }
        checkNotNull(account) { "Instanced with a NULL ownCloud Account" }
        check(file.isAvailableLocally) { "There is no local file to preview" }
        check(file.isAudio) { "Not an audio file" }
        extractAndSetCoverArt(file)
    }


    private fun extractAndSetCoverArt(file: OCFile) {
        try {
            val mediaMetadataRetriever = MediaMetadataRetriever()
            mediaMetadataRetriever.setDataSource(file.storagePath)
            val data = mediaMetadataRetriever.embeddedPicture
            if (data != null) {
                val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
                imagePreview?.setImageBitmap(bitmap) //associated cover art in bitmap
            } else {
                imagePreview?.setImageResource(R.drawable.ic_place_holder_music_cover_art)
            }
        } catch (t: Throwable) {
            imagePreview?.setImageResource(R.drawable.ic_place_holder_music_cover_art)
        }
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Timber.v("onSaveInstanceState")
        outState.putParcelable(EXTRA_FILE, file)
        outState.putParcelable(EXTRA_ACCOUNT, account)
        if (mediaServiceBinder != null) {
            outState.putInt(EXTRA_PLAY_POSITION, mediaServiceBinder!!.currentPosition)
            outState.putBoolean(EXTRA_PLAYING, mediaServiceBinder!!.isPlaying)
        }
    }

    override fun onStart() {
        super.onStart()
        Timber.v("onStart")
        val file = file
        if (file != null && file.isAvailableLocally) {
            bindMediaService()
        }
        isOpen = true
        currentFilePreviewing = file
    }

    override fun onFileMetadataChanged(updatedFile: OCFile?) {
        if (updatedFile != null) {
            file = updatedFile
        }
        requireActivity().invalidateOptionsMenu()
    }

    override fun onFileMetadataChanged() {
        mContainerActivity.storageManager?.let {
            file = it.getFileByPath(file.remotePath)
        }
        requireActivity().invalidateOptionsMenu()
    }

    override fun onFileContentChanged() {
        playAudio(true)
    }

    override fun updateViewForSyncInProgress() {

    }

    override fun updateViewForSyncOff() {

    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.file_actions_menu, menu)
    }


    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val safeFile = file
        val accountName = account!!.name
        previewAudioViewModel.filterMenuOptions(safeFile, accountName)

        collectLatestLifecycleFlow(previewAudioViewModel.menuOptions) { menuOptions ->
            val hasWritePermission = safeFile.hasWritePermission
            menu.filterMenuOptions(menuOptions, hasWritePermission)
        }

        menu.findItem(R.id.action_search)?.apply {
            isVisible = false
            isEnabled = false
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_share_file -> {
                mContainerActivity.fileOperationsHelper.showShareFile(file)
                true
            }

            R.id.action_open_file_with -> {
                openFile()
                true
            }

            R.id.action_remove_file -> {
                val dialog = RemoveFilesDialogFragment.newInstance(file)
                dialog.show(parentFragmentManager, TAG_REMOVE_FILES_DIALOG_FRAGMENT)
                true
            }

            R.id.action_see_details -> {
                seeDetails()
                true
            }

            R.id.action_send_file -> {
                requireActivity().sendDownloadedFilesByShareSheet(listOf(file))
                true
            }

            R.id.action_sync_file -> {
                mContainerActivity.fileOperationsHelper.syncFile(file)
                true
            }

            R.id.action_set_available_offline -> {
                fileOperationsViewModel.performOperation(FileOperation.SetFilesAsAvailableOffline(listOf(file)))
                true
            }

            R.id.action_unset_available_offline -> {
                fileOperationsViewModel.performOperation(FileOperation.UnsetFilesAsAvailableOffline(listOf(file)))
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun seeDetails() {
        mContainerActivity.showDetails(file)
    }

    override fun onStop() {
        mediaServiceConnection?.let { mediaConnection ->
            Timber.d("Unbinding from MediaService ...")
            if (mediaServiceBinder != null && mediaController != null) {
                mediaServiceBinder?.unregisterMediaController(mediaController)
            }
            requireActivity().unbindService(mediaConnection)
            mediaServiceConnection = null
            mediaServiceBinder = null
        }
        isOpen = false
        currentFilePreviewing = null
        super.onStop()
    }

    fun playAudio(restart: Boolean) {
        val file = file
        if (restart) {
            Timber.d("restarting playback of %s", file.storagePath)
            autoplay = true
            savedPlaybackPosition = 0
            mediaServiceBinder?.start(account, file, true, 0)
        } else if (mediaServiceBinder?.isPlaying(file) == false) {
            Timber.d("starting playback of %s", file.storagePath)
            mediaServiceBinder?.start(account, file, autoplay, savedPlaybackPosition)
        } else if (mediaServiceBinder?.isPlaying == false && autoplay) {
            mediaServiceBinder?.start()
            mediaController?.updatePausePlay()
        }
    }

    private fun bindMediaService() {
        Timber.d("Binding to MediaService...")
        if (mediaServiceConnection == null) {
            mediaServiceConnection = MediaServiceConnection().also {
                activity?.bindService(Intent(activity, MediaService::class.java), it, Context.BIND_AUTO_CREATE)
            }

        }
    }


    private inner class MediaServiceConnection : ServiceConnection {
        override fun onServiceConnected(component: ComponentName, service: IBinder) {
            activity?.let { fragmentActivity ->
                if (component == ComponentName(fragmentActivity, MediaService::class.java)) {
                    Timber.d("Media service connected")
                    mediaServiceBinder = service as MediaServiceBinder
                    if (mediaServiceBinder != null) {
                        prepareMediaController()
                        playAudio(false)
                        Timber.d("Successfully bound to MediaService, MediaController ready")
                    } else {
                        Timber.e("Unexpected response from MediaService while binding")
                    }
                }
            }
        }

        private fun prepareMediaController() {
            mediaServiceBinder?.registerMediaController(mediaController)
            mediaController?.let {
                it.setMediaPlayer(mediaServiceBinder)
                it.isEnabled = true
                it.updatePausePlay()
            }
        }

        override fun onServiceDisconnected(component: ComponentName) {
            if (component == ComponentName(requireActivity(), MediaService::class.java)) {
                Timber.w("Media service suddenly disconnected")
                if (mediaController != null) {
                    mediaController!!.setMediaPlayer(null)
                } else {
                    Timber.w("No media controller to release when disconnected from media service")
                }
                mediaServiceBinder = null
                mediaServiceConnection = null
            }
        }
    }


    private fun openFile() {
        stopPreview()
        mContainerActivity.fileOperationsHelper.openFile(file)
        finish()
    }

    fun stopPreview() {
        mediaServiceBinder?.pause()
    }


    private fun finish() {
        activity?.onBackPressed()
    }

    companion object {
        const val EXTRA_FILE = "FILE"
        const val EXTRA_ACCOUNT = "ACCOUNT"
        private const val EXTRA_PLAY_POSITION = "PLAY_POSITION"
        private const val EXTRA_PLAYING = "PLAYING"
        var isOpen: Boolean = false
        var currentFilePreviewing: OCFile? = null


        fun newInstance(
            file: OCFile?,
            account: Account?,
            startPlaybackPosition: Int,
            autoplay: Boolean
        ): PreviewAudioFragment {
            val args = Bundle().apply {
                putParcelable(EXTRA_FILE, file)
                putParcelable(EXTRA_ACCOUNT, account)
                putInt(EXTRA_PLAY_POSITION, startPlaybackPosition)
                putBoolean(EXTRA_PLAYING, autoplay)
            }

            return PreviewAudioFragment().apply {
                arguments = args
            }

        }


        @JvmStatic
        fun canBePreviewed(file: OCFile?) = file != null && file.isAvailableLocally && file.isAudio
    }
}
