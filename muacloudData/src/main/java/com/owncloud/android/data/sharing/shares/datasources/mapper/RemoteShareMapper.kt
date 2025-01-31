

package com.owncloud.android.data.sharing.shares.datasources.mapper

import com.owncloud.android.domain.mappers.RemoteMapper
import com.owncloud.android.domain.sharing.shares.model.OCShare
import com.owncloud.android.domain.sharing.shares.model.ShareType
import com.owncloud.android.lib.resources.shares.RemoteShare
import com.owncloud.android.lib.resources.shares.ShareType as RemoteShareType

class RemoteShareMapper : RemoteMapper<OCShare, RemoteShare> {
    override fun toModel(remote: RemoteShare?): OCShare? =
        remote?.let {
            OCShare(
                shareType = ShareType.fromValue(remote.shareType!!.value)!!,
                shareWith = remote.shareWith,
                path = remote.path,
                permissions = remote.permissions,
                sharedDate = remote.sharedDate,
                expirationDate = remote.expirationDate,
                token = remote.token,
                sharedWithDisplayName = remote.sharedWithDisplayName,
                sharedWithAdditionalInfo = remote.sharedWithAdditionalInfo,
                isFolder = remote.isFolder,
                remoteId = remote.id,
                name = remote.name,
                shareLink = remote.shareLink
            )
        }

    override fun toRemote(model: OCShare?): RemoteShare? =
        model?.let {
            RemoteShare(
                id = model.remoteId,
                shareWith = model.shareWith!!,
                path = model.path,
                token = model.token!!,
                sharedWithDisplayName = model.sharedWithDisplayName!!,
                sharedWithAdditionalInfo = model.sharedWithAdditionalInfo!!,
                name = model.name!!,
                shareLink = model.shareLink!!,
                shareType = RemoteShareType.fromValue(model.shareType.value),
                permissions = model.permissions,
                sharedDate = model.sharedDate,
                expirationDate = model.expirationDate,
                isFolder = model.isFolder,
            )
        }
}
