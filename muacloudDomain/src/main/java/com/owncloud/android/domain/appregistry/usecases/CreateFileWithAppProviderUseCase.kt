

package com.owncloud.android.domain.appregistry.usecases

import com.owncloud.android.domain.BaseUseCaseWithResult
import com.owncloud.android.domain.appregistry.AppRegistryRepository
import com.owncloud.android.domain.capabilities.CapabilityRepository

class CreateFileWithAppProviderUseCase(
    private val capabilitiesRepository: CapabilityRepository,
    private val appRegistryRepository: AppRegistryRepository,
) : BaseUseCaseWithResult<String, CreateFileWithAppProviderUseCase.Params>() {

    override fun run(params: Params): String {
        val capabilities = capabilitiesRepository.getStoredCapabilities(params.accountName)
        val createFileWithAppProviderUrl = capabilities?.filesAppProviders?.newUrl

        requireNotNull(createFileWithAppProviderUrl)

        return appRegistryRepository.createFileWithAppProvider(
            accountName = params.accountName,
            createFileWithAppProviderEndpoint = createFileWithAppProviderUrl,
            parentContainerId = params.parentContainerId,
            filename = params.filename,
        )
    }

    data class Params(
        val accountName: String,
        val parentContainerId: String,
        val filename: String,
    )
}
