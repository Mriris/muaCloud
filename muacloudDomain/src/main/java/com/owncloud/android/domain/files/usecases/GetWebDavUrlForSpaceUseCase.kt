

package com.owncloud.android.domain.files.usecases

import com.owncloud.android.domain.BaseUseCase
import com.owncloud.android.domain.spaces.SpacesRepository

class GetWebDavUrlForSpaceUseCase(
    private val spacesRepository: SpacesRepository
) : BaseUseCase<String?, GetWebDavUrlForSpaceUseCase.Params>() {

    override fun run(params: Params): String? = spacesRepository.getWebDavUrlForSpace(
        accountName = params.accountName,
        spaceId = params.spaceId,
    )

    data class Params(
        val accountName: String,
        val spaceId: String?,
    )
}
