package com.owncloud.android.lib.resources.spaces.services

import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.resources.spaces.GetRemoteSpacesOperation
import com.owncloud.android.lib.resources.spaces.responses.SpaceResponse

class OCSpacesService(override val client: OwnCloudClient) : SpacesService {
    override fun getSpaces(): RemoteOperationResult<List<SpaceResponse>> {
        return GetRemoteSpacesOperation().execute(client)
    }
}
