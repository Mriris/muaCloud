

package com.owncloud.android.domain.capabilities.usecases

import com.owncloud.android.domain.capabilities.CapabilityRepository
import com.owncloud.android.domain.capabilities.model.OCCapability
import com.owncloud.android.domain.BaseUseCase

class GetStoredCapabilitiesUseCase(
    private val capabilityRepository: CapabilityRepository
) : BaseUseCase<OCCapability?, GetStoredCapabilitiesUseCase.Params>() {
    override fun run(params: Params): OCCapability? =
        capabilityRepository.getStoredCapabilities(params.accountName)

    data class Params(
        val accountName: String
    )
}
