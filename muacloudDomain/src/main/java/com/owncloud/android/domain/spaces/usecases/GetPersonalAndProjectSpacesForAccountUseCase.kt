

package com.owncloud.android.domain.spaces.usecases

import com.owncloud.android.domain.BaseUseCase
import com.owncloud.android.domain.spaces.SpacesRepository
import com.owncloud.android.domain.spaces.model.OCSpace

class GetPersonalAndProjectSpacesForAccountUseCase(
    private val spacesRepository: SpacesRepository
) : BaseUseCase<List<OCSpace>, GetPersonalAndProjectSpacesForAccountUseCase.Params>() {

    override fun run(params: Params) = spacesRepository.getPersonalAndProjectSpacesForAccount(params.accountName)

    data class Params(
        val accountName: String
    )
}
