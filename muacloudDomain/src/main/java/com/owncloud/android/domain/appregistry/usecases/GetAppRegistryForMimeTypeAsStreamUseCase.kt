
package com.owncloud.android.domain.appregistry.usecases

import com.owncloud.android.domain.BaseUseCase
import com.owncloud.android.domain.appregistry.AppRegistryRepository
import com.owncloud.android.domain.appregistry.model.AppRegistryMimeType
import kotlinx.coroutines.flow.Flow

class GetAppRegistryForMimeTypeAsStreamUseCase(
    private val appRegistryRepository: AppRegistryRepository,
) : BaseUseCase<Flow<AppRegistryMimeType?>, GetAppRegistryForMimeTypeAsStreamUseCase.Params>() {

    override fun run(params: Params) =
        appRegistryRepository.getAppRegistryForMimeTypeAsStream(accountName = params.accountName, mimeType = params.mimeType)

    data class Params(
        val accountName: String,
        val mimeType: String,
    )
}
