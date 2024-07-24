
package com.owncloud.android.domain.appregistry.usecases

import com.owncloud.android.domain.BaseUseCaseWithResult
import com.owncloud.android.domain.appregistry.AppRegistryRepository
import com.owncloud.android.domain.capabilities.CapabilityRepository

class GetUrlToOpenInWebUseCase(
    private val capabilitiesRepository: CapabilityRepository,
    private val appRegistryRepository: AppRegistryRepository,
) : BaseUseCaseWithResult<String, GetUrlToOpenInWebUseCase.Params>() {

    override fun run(params: Params): String {
        val capabilities = capabilitiesRepository.getStoredCapabilities(params.accountName)
        val openInWebUrl = capabilities?.filesAppProviders?.openWebUrl

        requireNotNull(openInWebUrl)

        return appRegistryRepository.getUrlToOpenInWeb(
            accountName = params.accountName,
            openWebEndpoint = openInWebUrl,
            fileId = params.fileId,
            appName = params.appName,
        )
    }

    data class Params(
        val accountName: String,
        val fileId: String,
        val appName: String,
    )
}
