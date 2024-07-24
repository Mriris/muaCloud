

package com.owncloud.android.domain.spaces.usecases

import com.owncloud.android.domain.BaseUseCase
import com.owncloud.android.domain.spaces.SpacesRepository
import com.owncloud.android.domain.spaces.model.OCSpace

class GetPersonalSpaceForAccountUseCase(
    private val spacesRepository: SpacesRepository
) : BaseUseCase<OCSpace?, GetPersonalSpaceForAccountUseCase.Params>() {

    override fun run(params: Params) = spacesRepository.getPersonalSpaceForAccount(params.accountName)

    data class Params(
        val accountName: String
    )
}
