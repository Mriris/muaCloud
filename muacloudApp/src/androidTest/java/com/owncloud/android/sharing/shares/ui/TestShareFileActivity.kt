

package com.owncloud.android.sharing.shares.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.owncloud.android.R
import com.owncloud.android.domain.files.model.OCFile
import com.owncloud.android.domain.sharing.shares.model.OCShare
import com.owncloud.android.presentation.sharing.ShareFragmentListener
import com.owncloud.android.services.OperationsService
import com.owncloud.android.testing.SingleFragmentActivity
import com.owncloud.android.ui.fragment.FileFragment.ContainerActivity
import com.owncloud.android.ui.helpers.FileOperationsHelper

class TestShareFileActivity : SingleFragmentActivity(), ShareFragmentListener, ContainerActivity {
    fun startFragment(fragment: Fragment) {
        supportFragmentManager.commit(allowStateLoss = true) {
            add(R.id.container, fragment, TEST_FRAGMENT_TAG)
        }
    }

    fun getTestFragment(): Fragment? = supportFragmentManager.findFragmentByTag(TEST_FRAGMENT_TAG)

    override fun copyOrSendPrivateLink(file: OCFile) {
    }

    override fun deleteShare(remoteId: String) {
    }

    override fun showLoading() {
    }

    override fun dismissLoading() {
    }

    override fun showAddPublicShare(defaultLinkName: String) {
    }

    override fun showEditPublicShare(share: OCShare) {
    }

    override fun showRemoveShare(share: OCShare) {
    }

    override fun copyOrSendPublicLink(share: OCShare) {
    }

    override fun showSearchUsersAndGroups() {
    }

    override fun showEditPrivateShare(share: OCShare) {
    }

    companion object {
        private const val TEST_FRAGMENT_TAG = "TEST FRAGMENT"
    }

    override fun getOperationsServiceBinder(): OperationsService.OperationsServiceBinder {
        TODO("Not yet implemented")
    }

    override fun getFileOperationsHelper(): FileOperationsHelper {
        TODO("Not yet implemented")
    }

    override fun showDetails(file: OCFile?) {
    }
}
