

package com.owncloud.android.domain.server.usecases

import com.owncloud.android.domain.BaseUseCaseWithResult
import com.owncloud.android.domain.exceptions.NOT_HTTP_ALLOWED_MESSAGE
import com.owncloud.android.domain.exceptions.SSLErrorCode
import com.owncloud.android.domain.exceptions.SSLErrorException
import com.owncloud.android.domain.server.ServerInfoRepository
import com.owncloud.android.domain.server.model.ServerInfo
import com.owncloud.android.domain.server.model.ServerInfo.Companion.HTTPS_PREFIX
import com.owncloud.android.domain.server.model.ServerInfo.Companion.HTTP_PREFIX
import java.util.Locale

class GetServerInfoAsyncUseCase(
    private val serverInfoRepository: ServerInfoRepository,
) : BaseUseCaseWithResult<ServerInfo, GetServerInfoAsyncUseCase.Params>() {
    override fun run(params: Params): ServerInfo {
        val normalizedServerUrl = normalizeProtocolPrefix(params.serverPath).trimEnd(TRAILING_SLASH)
        val serverInfo = serverInfoRepository.getServerInfo(normalizedServerUrl, params.creatingAccount)
        if (!serverInfo.isSecureConnection && params.secureConnectionEnforced) {
            throw SSLErrorException(NOT_HTTP_ALLOWED_MESSAGE, SSLErrorCode.NOT_HTTP_ALLOWED)
        }
        return serverInfo
    }

    data class Params(
        val serverPath: String,
        val creatingAccount: Boolean,
        val secureConnectionEnforced: Boolean,
    )


    private fun normalizeProtocolPrefix(url: String): String {
        return if (!url.lowercase(Locale.getDefault()).startsWith(HTTP_PREFIX) &&
            !url.lowercase(Locale.getDefault()).startsWith(HTTPS_PREFIX)
        ) {
            return "$HTTPS_PREFIX$url"
        } else url
    }

    companion object {
        const val TRAILING_SLASH = '/'
    }
}
