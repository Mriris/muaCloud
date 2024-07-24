

package com.owncloud.android.domain.appregistry.usecases

import com.owncloud.android.domain.BaseUseCase
import com.owncloud.android.domain.appregistry.AppRegistryRepository
import com.owncloud.android.domain.appregistry.model.AppRegistryMimeType
import kotlinx.coroutines.flow.Flow

class GetAppRegistryWhichAllowCreationAsStreamUseCase(
    private val appRegistryRepository: AppRegistryRepository,
) : BaseUseCase<Flow<List<AppRegistryMimeType>>, GetAppRegistryWhichAllowCreationAsStreamUseCase.Params>() {

    override fun run(params: Params) =
        appRegistryRepository.getAppRegistryWhichAllowCreation(accountName = params.accountName)

    data class Params(
        val accountName: String,
    )
}
