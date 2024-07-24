

package com.owncloud.android.domain.spaces.usecases

import com.owncloud.android.domain.BaseUseCase
import com.owncloud.android.domain.spaces.SpacesRepository
import com.owncloud.android.domain.spaces.model.OCSpace

class GetSpaceWithSpecialsByIdForAccountUseCase(
    private val spacesRepository: SpacesRepository
) : BaseUseCase<OCSpace?, GetSpaceWithSpecialsByIdForAccountUseCase.Params>() {

    override fun run(params: Params): OCSpace? {
        if (params.spaceId == null) return null
        return spacesRepository.getSpaceWithSpecialsByIdForAccount(params.spaceId, params.accountName)
    }

    data class Params(
        val spaceId: String?,
        val accountName: String,
    )
}
