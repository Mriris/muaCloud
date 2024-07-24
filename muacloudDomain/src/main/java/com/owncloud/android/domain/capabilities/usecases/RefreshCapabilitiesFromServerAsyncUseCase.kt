

package com.owncloud.android.domain.capabilities.usecases

import com.owncloud.android.domain.capabilities.CapabilityRepository
import com.owncloud.android.domain.BaseUseCaseWithResult

class RefreshCapabilitiesFromServerAsyncUseCase(
    private val capabilityRepository: CapabilityRepository
) : BaseUseCaseWithResult<Unit, RefreshCapabilitiesFromServerAsyncUseCase.Params>() {

    override fun run(params: Params) =
        capabilityRepository.refreshCapabilitiesForAccount(
            params.accountName
        )

    data class Params(
        val accountName: String
    )
}
