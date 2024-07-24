

package com.owncloud.android.domain.capabilities.usecases

import androidx.lifecycle.LiveData
import com.owncloud.android.domain.BaseUseCase
import com.owncloud.android.domain.capabilities.CapabilityRepository
import com.owncloud.android.domain.capabilities.model.OCCapability

class GetCapabilitiesAsLiveDataUseCase(
    private val capabilityRepository: CapabilityRepository
) : BaseUseCase<LiveData<OCCapability?>, GetCapabilitiesAsLiveDataUseCase.Params>() {

    override fun run(params: Params): LiveData<OCCapability?> = capabilityRepository.getCapabilitiesAsLiveData(
        params.accountName
    )

    data class Params(
        val accountName: String
    )
}
