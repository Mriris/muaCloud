

package com.owncloud.android.domain.webfinger.usecases

import androidx.core.net.toUri
import com.owncloud.android.domain.BaseUseCaseWithResult
import com.owncloud.android.domain.webfinger.WebFingerRepository
import com.owncloud.android.domain.webfinger.model.WebFingerRel

class GetOwnCloudInstancesFromAuthenticatedWebFingerUseCase(
    private val webFingerRepository: WebFingerRepository
) : BaseUseCaseWithResult<List<String>, GetOwnCloudInstancesFromAuthenticatedWebFingerUseCase.Params>() {

    override fun run(params: Params): List<String> =
        webFingerRepository.getInstancesFromAuthenticatedWebFinger(
            server = params.server,
            rel = WebFingerRel.OWNCLOUD_INSTANCE,
            resource = getResourceForAuthenticatedWebFinger(params.server),
            username = params.username,
            accessToken = params.accessToken,
        )

    private fun getResourceForAuthenticatedWebFinger(serverUrl: String): String {
        val host = serverUrl.toUri().host
        return "$PREFIX_ACCT_URI_SCHEME:me@$host"
    }

    data class Params(
        val server: String,
        val username: String,
        val accessToken: String,
    )

    companion object {

        private const val PREFIX_ACCT_URI_SCHEME = "acct"
    }
}
