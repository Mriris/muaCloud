
package com.owncloud.android.domain.spaces.usecases

import com.owncloud.android.domain.BaseUseCaseWithResult
import com.owncloud.android.domain.spaces.SpacesRepository

class RefreshSpacesFromServerAsyncUseCase(
    private val spacesRepository: SpacesRepository
) : BaseUseCaseWithResult<Unit, RefreshSpacesFromServerAsyncUseCase.Params>() {

    override fun run(params: Params) =
        spacesRepository.refreshSpacesForAccount(accountName = params.accountName)

    data class Params(
        val accountName: String,
    )
}
