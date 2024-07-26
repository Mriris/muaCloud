

package com.owncloud.android.presentation.sharing

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.transaction
import com.owncloud.android.R
import com.owncloud.android.domain.files.model.OCFile
import com.owncloud.android.domain.sharing.shares.model.OCShare
import com.owncloud.android.domain.sharing.shares.model.ShareType
import com.owncloud.android.domain.utils.Event.EventObserver
import com.owncloud.android.extensions.showErrorInSnackbar
import com.owncloud.android.lib.resources.shares.RemoteShare
import com.owncloud.android.presentation.common.UIResult
import com.owncloud.android.presentation.sharing.sharees.EditPrivateShareFragment
import com.owncloud.android.presentation.sharing.sharees.SearchShareesFragment
import com.owncloud.android.presentation.sharing.sharees.UsersAndGroupsSearchProvider
import com.owncloud.android.presentation.sharing.shares.PublicShareDialogFragment
import com.owncloud.android.ui.activity.FileActivity
import com.owncloud.android.ui.utils.showDialogFragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import timber.log.Timber


class ShareActivity : FileActivity(), ShareFragmentListener {
    private val shareViewModel: ShareViewModel by viewModel {
        parametersOf(
            file.remotePath,
            account?.name
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.share_activity)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeActionContentDescription(R.string.common_back)
        supportFragmentManager.transaction {
            if (savedInstanceState == null && file != null && account != null) {

                val fragment = ShareFileFragment.newInstance(file, account!!)
                replace(
                    R.id.share_fragment_container, fragment,
                    TAG_SHARE_FRAGMENT
                )
            }
        }

        observePrivateShareCreation()
        observePrivateShareEdition()
        observeShareDeletion()
    }

    
    override fun showSearchUsersAndGroups() {
        supportFragmentManager.transaction {
            val searchFragment = SearchShareesFragment.newInstance(file, account)
            replace(
                R.id.share_fragment_container,
                searchFragment,
                TAG_SEARCH_FRAGMENT
            )
            addToBackStack(null)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        when (intent.action) {
            Intent.ACTION_SEARCH -> {  // Verify the action and get the query
                val query = intent.getStringExtra(SearchManager.QUERY)
                Timber.w("Ignored Intent requesting to query for $query")
            }
            UsersAndGroupsSearchProvider.suggestIntentAction -> {
                val data = intent.data
                val dataString = intent.dataString
                val shareWith = dataString!!.substring(dataString.lastIndexOf('/') + 1)
                createPrivateShare(
                    shareWith,
                    data?.authority
                )
            }
            else -> Timber.e("Unexpected intent $intent")
        }
    }

    private fun createPrivateShare(shareeName: String, dataAuthority: String?) {
        val shareType = UsersAndGroupsSearchProvider.getShareType(dataAuthority)

        shareViewModel.insertPrivateShare(
            file.remotePath,
            shareType,
            shareeName,
            getAppropriatePermissions(shareType),
            account.name
        )
    }

    private fun observePrivateShareCreation() {
        shareViewModel.privateShareCreationStatus.observe(
            this,
            EventObserver { uiResult ->
                when (uiResult) {
                    is UIResult.Error -> {
                        showErrorInSnackbar(R.string.share_link_file_error, uiResult.error)
                        dismissLoadingDialog()
                    }
                    is UIResult.Loading -> {
                        showLoadingDialog(R.string.common_loading)
                    }
                    is UIResult.Success -> {}
                }
            }
        )
    }

    private fun getAppropriatePermissions(shareType: ShareType?): Int {

        val isFederated = ShareType.FEDERATED == shareType

        return when {
            file.isSharedWithMe -> RemoteShare.READ_PERMISSION_FLAG    // minimum permissions
            isFederated ->
                if (file.isFolder) {
                    RemoteShare.FEDERATED_PERMISSIONS_FOR_FOLDER
                } else {
                    RemoteShare.FEDERATED_PERMISSIONS_FOR_FILE
                }
            else ->
                if (file.isFolder) {
                    RemoteShare.MAXIMUM_PERMISSIONS_FOR_FOLDER
                } else {
                    RemoteShare.MAXIMUM_PERMISSIONS_FOR_FILE
                }
        }
    }

    private fun observePrivateShareEdition() {
        shareViewModel.privateShareEditionStatus.observe(
            this,
            EventObserver { uiResult ->
                when (uiResult) {
                    is UIResult.Error -> {
                        showErrorInSnackbar(R.string.share_link_file_error, uiResult.error)
                        dismissLoadingDialog()
                    }
                    is UIResult.Loading -> {
                        showLoadingDialog(R.string.common_loading)
                    }
                    is UIResult.Success -> {}
                }
            }
        )
    }

    override fun showEditPrivateShare(share: OCShare) {
        val ft = supportFragmentManager.beginTransaction()
        val prev = supportFragmentManager.findFragmentByTag(TAG_EDIT_SHARE_FRAGMENT)
        if (prev != null) {
            ft.remove(prev)    // BACK button will recover the previous fragment
        }
        ft.addToBackStack(null)

        val newFragment = EditPrivateShareFragment.newInstance(share, file, account)
        newFragment.show(
            ft,
            TAG_EDIT_SHARE_FRAGMENT
        )
    }

    override fun copyOrSendPrivateLink(file: OCFile) {
        fileOperationsHelper.copyOrSendPrivateLink(file)
    }

    
    override fun showAddPublicShare(defaultLinkName: String) {




        val createPublicShareFragment = PublicShareDialogFragment.newInstanceToCreate(
            file,
            account,
            defaultLinkName
        )

        showDialogFragment(
            createPublicShareFragment,
            TAG_PUBLIC_SHARE_DIALOG_FRAGMENT
        )
    }

    override fun showEditPublicShare(share: OCShare) {

        val editPublicShareFragment = PublicShareDialogFragment.newInstanceToUpdate(file, account, share)
        showDialogFragment(
            editPublicShareFragment,
            TAG_PUBLIC_SHARE_DIALOG_FRAGMENT
        )
    }

    
    private fun observeShareDeletion() {
        shareViewModel.shareDeletionStatus.observe(
            this,
            EventObserver { uiResult ->
                when (uiResult) {
                    is UIResult.Error -> {
                        dismissLoadingDialog()
                        showErrorInSnackbar(R.string.unshare_link_file_error, uiResult.error)
                    }
                    is UIResult.Loading -> {
                        showLoading()
                    }
                    is UIResult.Success -> {}
                }
            }
        )
    }

    override fun showRemoveShare(share: OCShare) {
        val removePublicShareFragment = RemoveShareDialogFragment.newInstance(share, account)
        showDialogFragment(
            removePublicShareFragment,
            TAG_REMOVE_SHARE_DIALOG_FRAGMENT
        )
    }

    override fun deleteShare(remoteId: String) {
        shareViewModel.deleteShare(remoteId)
    }

    override fun copyOrSendPublicLink(share: OCShare) {
        fileOperationsHelper.copyOrSendPublicLink(share)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var retval = true
        when (item.itemId) {
            android.R.id.home -> if (!supportFragmentManager.popBackStackImmediate()) {
                finish()
            }
            else -> retval = super.onOptionsItemSelected(item)
        }
        return retval
    }

    override fun showLoading() {
        showLoadingDialog(R.string.common_loading)
    }

    override fun dismissLoading() {
        dismissLoadingDialog()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return false
    }

    companion object {
        const val TAG_SHARE_FRAGMENT = "SHARE_FRAGMENT"
        const val TAG_SEARCH_FRAGMENT = "SEARCH_USER_AND_GROUPS_FRAGMENT"
        const val TAG_EDIT_SHARE_FRAGMENT = "EDIT_SHARE_FRAGMENT"
        const val TAG_PUBLIC_SHARE_DIALOG_FRAGMENT = "PUBLIC_SHARE_DIALOG_FRAGMENT"
        const val TAG_REMOVE_SHARE_DIALOG_FRAGMENT = "REMOVE_SHARE_DIALOG_FRAGMENT"
    }
}
