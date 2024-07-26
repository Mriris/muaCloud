package com.owncloud.android.lib.resources.spaces.services

import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.resources.Service
import com.owncloud.android.lib.resources.spaces.responses.SpaceResponse

interface SpacesService : Service {
    fun getSpaces(): RemoteOperationResult<List<SpaceResponse>>
}
