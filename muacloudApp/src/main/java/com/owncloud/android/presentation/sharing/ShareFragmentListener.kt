

package com.owncloud.android.presentation.sharing

import com.owncloud.android.domain.files.model.OCFile
import com.owncloud.android.domain.sharing.shares.model.OCShare


interface ShareFragmentListener {
    fun copyOrSendPrivateLink(file: OCFile)

    fun deleteShare(remoteId: String)

    fun showLoading()

    fun dismissLoading()


    fun showAddPublicShare(defaultLinkName: String)

    fun showEditPublicShare(share: OCShare)

    fun showRemoveShare(share: OCShare)

    fun copyOrSendPublicLink(share: OCShare)


    fun showSearchUsersAndGroups()

    fun showEditPrivateShare(share: OCShare)
}
