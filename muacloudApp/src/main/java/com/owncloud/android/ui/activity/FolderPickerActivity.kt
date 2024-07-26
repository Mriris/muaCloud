

package com.owncloud.android.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import com.owncloud.android.R
import com.owncloud.android.databinding.FilesFolderPickerBinding
import com.owncloud.android.datamodel.FileDataStorageManager
import com.owncloud.android.domain.files.model.FileListOption
import com.owncloud.android.domain.files.model.OCFile
import com.owncloud.android.domain.spaces.model.OCSpace
import com.owncloud.android.presentation.authentication.AccountUtils
import com.owncloud.android.presentation.files.filelist.MainFileListFragment
import com.owncloud.android.presentation.spaces.SpacesListFragment
import com.owncloud.android.ui.fragment.FileFragment
import com.owncloud.android.utils.PreferenceUtils
import timber.log.Timber

open class FolderPickerActivity : FileActivity(),
    FileFragment.ContainerActivity,
    MainFileListFragment.FileActions {

    protected val mainFileListFragment: MainFileListFragment?
        get() = supportFragmentManager.findFragmentByTag(TAG_LIST_OF_FOLDERS) as MainFileListFragment?

    private lateinit var pickerMode: PickerMode

    private lateinit var binding: FilesFolderPickerBinding



    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.d("onCreate() start")

        super.onCreate(savedInstanceState)

        binding = FilesFolderPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.filesFolderPickerLayout.filterTouchesWhenObscured = PreferenceUtils.shouldDisallowTouchesWithOtherVisibleWindows(this)

        pickerMode = intent.getSerializableExtra(EXTRA_PICKER_MODE) as PickerMode

        if (savedInstanceState == null) {
            when (pickerMode) {
                PickerMode.MOVE -> {

                    val targetFiles = intent.getParcelableArrayListExtra<OCFile>(EXTRA_FILES)
                    val spaceIdOfFiles = targetFiles?.get(0)?.spaceId
                    initAndShowListOfFilesFragment(spaceId = spaceIdOfFiles)
                }
                PickerMode.COPY -> {
                    val targetFiles = intent.getParcelableArrayListExtra<OCFile>(EXTRA_FILES)
                    if (targetFiles?.get(0)?.spaceId != null) {

                        initAndShowListOfSpaces()
                    } else {

                        initAndShowListOfFilesFragment(spaceId = null)
                    }
                }
                PickerMode.CAMERA_FOLDER -> {
                    val spaceId = intent.getStringExtra(KEY_SPACE_ID)

                    if (spaceId != null) {

                        initAndShowListOfSpaces()
                    } else {
                        val accountName = intent.getStringExtra(KEY_ACCOUNT_NAME)
                        account = AccountUtils.getOwnCloudAccountByName(this, accountName)

                        initAndShowListOfFilesFragment(spaceId = null)
                    }
                }
            }
        }

        initPickerListeners()

        setupStandardToolbar(
            title = null,
            displayHomeAsUpEnabled = false,
            homeButtonEnabled = false,
            displayShowTitleEnabled = true,
        )

        setActionButtonText()

        supportFragmentManager.setFragmentResultListener(SpacesListFragment.REQUEST_KEY_CLICK_SPACE, this) { _, bundle ->
            val rootSpaceFolder = bundle.getParcelable<OCFile>(SpacesListFragment.BUNDLE_KEY_CLICK_SPACE)
            file = rootSpaceFolder
            initAndShowListOfFilesFragment()
        }

        Timber.d("onCreate() end")
    }

    override fun onResume() {
        super.onResume()
        updateToolbar(null, mainFileListFragment?.getCurrentSpace())
    }


    override fun onAccountSet(stateWasRecovered: Boolean) {
        super.onAccountSet(stateWasRecovered)

        if (account != null) {
            updateFileFromDB()

            var folder = file
            if (folder == null || !folder.isFolder) {

                file = storageManager.getRootPersonalFolder()
                folder = file
            }

            if (!stateWasRecovered) {
                mainFileListFragment?.navigateToFolder(folder)
            }

            updateNavigationElementsInActionBar()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menu.removeItem(menu.findItem(R.id.action_share_current_folder)?.itemId ?: 0)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        val currentDirDisplayed = mainFileListFragment?.getCurrentFile()

        if (currentDirDisplayed == null) {
            finish()
            return
        }

        else if (currentDirDisplayed.parentId == OCFile.ROOT_PARENT_ID) {

            if (pickerMode != PickerMode.COPY || (pickerMode == PickerMode.COPY && currentDirDisplayed.spaceId == null)) {
                finish()
                return
            }

            if (mainFileListFragment?.getCurrentSpace()?.isProject == true || mainFileListFragment?.getCurrentSpace()?.isPersonal == true) {
                file = null
                initAndShowListOfSpaces()
                updateToolbar(null)
                binding.folderPickerNoPermissionsMessage.isVisible = false
            }
        } else {
            mainFileListFragment?.onBrowseUp()
        }
    }

    override fun onCurrentFolderUpdated(newCurrentFolder: OCFile, currentSpace: OCSpace?) {
        updateToolbar(newCurrentFolder, currentSpace)
        updateButtonsVisibilityAccordingToPermissions(newCurrentFolder)
        file = newCurrentFolder
    }

    override fun initDownloadForSending(file: OCFile) {

    }

    override fun cancelFileTransference(files: ArrayList<OCFile>) {

    }

    override fun setBottomBarVisibility(isVisible: Boolean) {

    }

    override fun onFileClicked(file: OCFile) {

    }

    override fun onShareFileClicked(file: OCFile) {

    }

    override fun syncFile(file: OCFile) {

    }

    override fun openFile(file: OCFile) {

    }

    override fun sendDownloadedFile(file: OCFile) {

    }

    override fun showDetails(file: OCFile) {

    }

    private fun initAndShowListOfFilesFragment(spaceId: String? = null) {
        val safeInitialFolder = if (file == null) {
            if (account == null) {
                account = AccountUtils.getCurrentOwnCloudAccount(applicationContext)
            }
            val fileDataStorageManager = FileDataStorageManager(account)
            fileDataStorageManager.getFileByPath(OCFile.ROOT_PATH, spaceId)
        } else {
            file
        }

        file = safeInitialFolder

        safeInitialFolder?.let {
            val mainListOfFiles = MainFileListFragment.newInstance(it, true, FileListOption.ALL_FILES)
            mainListOfFiles.fileActions = this
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, mainListOfFiles, TAG_LIST_OF_FOLDERS)
            transaction.commit()
        }


        binding.folderPickerBtnChoose.isVisible = true
    }

    private fun initAndShowListOfSpaces() {
        val accountNameIntent = intent.getStringExtra(KEY_ACCOUNT_NAME)
        val accountName = accountNameIntent ?: AccountUtils.getCurrentOwnCloudAccount(applicationContext).name

        val listOfSpaces = SpacesListFragment.newInstance(showPersonalSpace = true, accountName = accountName)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, listOfSpaces)
        transaction.commit()
        binding.folderPickerBtnChoose.isVisible = false
    }


    private fun initPickerListeners() {

        binding.folderPickerBtnCancel.setOnClickListener {
            finish()
        }

        binding.folderPickerBtnChoose.setOnClickListener {
            val data = Intent().apply {
                val targetFiles = intent.getParcelableArrayListExtra<OCFile>(EXTRA_FILES)
                putExtra(EXTRA_FOLDER, getCurrentFolder())
                putParcelableArrayListExtra(EXTRA_FILES, targetFiles)
            }
            setResult(RESULT_OK, data)

            finish()
        }
    }

    private fun setActionButtonText() {
        binding.folderPickerBtnChoose.text = getString(pickerMode.toStringRes())
    }

    private fun getCurrentFolder(): OCFile? {
        if (mainFileListFragment != null) {
            return mainFileListFragment?.getCurrentFile()
        }
        return null
    }

    private fun updateToolbar(chosenFileFromParam: OCFile?, space: OCSpace? = null) {
        val chosenFile = chosenFileFromParam ?: file // If no file is passed, current file decides
        val isRootFromPersonalInCopyMode =
            chosenFile != null && chosenFile.remotePath == OCFile.ROOT_PATH && space?.isProject == false && pickerMode == PickerMode.COPY
        val isRootFromPersonal = chosenFile == null || (chosenFile.remotePath == OCFile.ROOT_PATH && (space == null || !space.isProject))
        val isRootFromProject = space?.isProject == true && chosenFile.remotePath == OCFile.ROOT_PATH

        if (isRootFromPersonalInCopyMode) {
            updateStandardToolbar(
                title = getString(R.string.default_display_name_for_root_folder),
                displayHomeAsUpEnabled = true,
                homeButtonEnabled = true
            )
        } else if (isRootFromPersonal) {
            updateStandardToolbar(
                title = getString(R.string.default_display_name_for_root_folder),
                displayHomeAsUpEnabled = false,
                homeButtonEnabled = false
            )
        } else if (isRootFromProject) {
            updateStandardToolbar(
                title = space!!.name,
                displayHomeAsUpEnabled = pickerMode == PickerMode.COPY,
                homeButtonEnabled = pickerMode == PickerMode.COPY
            )
        } else {
            updateStandardToolbar(title = chosenFile.fileName, displayHomeAsUpEnabled = true, homeButtonEnabled = true)
        }
    }

    private fun updateButtonsVisibilityAccordingToPermissions(currentFolder: OCFile) {
        currentFolder.hasAddFilePermission.let {
            binding.folderPickerBtnChoose.isVisible = it
            binding.folderPickerNoPermissionsMessage.isVisible = !it
        }
    }

    protected fun updateNavigationElementsInActionBar() {
        val currentDir = try {
            getCurrentFolder()
        } catch (e: NullPointerException) {
            file
        }

        val atRoot = (currentDir == null || currentDir.parentId == 0L)
        updateStandardToolbar(
            title = if (atRoot) getString(R.string.default_display_name_for_root_folder) else currentDir!!.fileName,
            displayHomeAsUpEnabled = !atRoot,
            homeButtonEnabled = !atRoot,
        )
    }

    enum class PickerMode {
        MOVE, COPY, CAMERA_FOLDER;

        @StringRes
        fun toStringRes(): Int {
            return when (this) {
                MOVE -> R.string.folder_picker_move_here_button_text
                COPY -> R.string.folder_picker_copy_here_button_text
                CAMERA_FOLDER -> R.string.folder_picker_choose_button_text
            }
        }
    }

    companion object {
        const val KEY_ACCOUNT_NAME = "KEY_ACCOUNT_NAME"
        const val KEY_SPACE_ID = "KEY_PERSONAL_SPACE_ID"
        const val EXTRA_FOLDER = "FOLDER_PICKER_EXTRA_FOLDER"
        const val EXTRA_FILES = "FOLDER_PICKER_EXTRA_FILES"
        const val EXTRA_PICKER_MODE = "FOLDER_PICKER_EXTRA_PICKER_MODE"
        private const val TAG_LIST_OF_FOLDERS = "LIST_OF_FOLDERS"
    }
}

