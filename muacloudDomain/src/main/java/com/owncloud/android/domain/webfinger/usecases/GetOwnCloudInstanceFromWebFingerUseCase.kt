

package com.owncloud.android.domain.webfinger.usecases

import com.owncloud.android.domain.BaseUseCaseWithResult
import com.owncloud.android.domain.webfinger.WebFingerRepository
import com.owncloud.android.domain.webfinger.model.WebFingerRel

class GetOwnCloudInstanceFromWebFingerUseCase(
    private val webfingerRepository: WebFingerRepository
) : BaseUseCaseWithResult<String, GetOwnCloudInstanceFromWebFingerUseCase.Params>() {

    override fun run(params: Params): String =
        webfingerRepository.getInstancesFromWebFinger(
            server = params.server,
            rel = WebFingerRel.OWNCLOUD_INSTANCE,
            resource = params.resource
        ).first()

    data class Params(
        val server: String,
        val resource: String
    )
}
