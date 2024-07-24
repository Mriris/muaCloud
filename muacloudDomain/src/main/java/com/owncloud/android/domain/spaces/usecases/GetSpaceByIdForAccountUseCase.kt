
package com.owncloud.android.domain.spaces.usecases

import com.owncloud.android.domain.BaseUseCase
import com.owncloud.android.domain.spaces.SpacesRepository
import com.owncloud.android.domain.spaces.model.OCSpace

class GetSpaceByIdForAccountUseCase(
    private val spacesRepository: SpacesRepository
) : BaseUseCase<OCSpace?, GetSpaceByIdForAccountUseCase.Params>() {

    override fun run(params: Params) = spacesRepository.getSpaceByIdForAccount(
        spaceId = params.spaceId,
        accountName = params.accountName
    )

    data class Params(
        val accountName: String,
        val spaceId: String?,
    )
}
