

package com.owncloud.android.domain.files.usecases

import com.owncloud.android.domain.BaseUseCaseWithResult
import com.owncloud.android.domain.exceptions.DeepLinkException
import com.owncloud.android.domain.files.FileRepository
import com.owncloud.android.domain.files.model.OCFile
import com.owncloud.android.domain.files.model.OCFile.Companion.PATH_SEPARATOR
import java.net.URI

class ManageDeepLinkUseCase(
    private val fileRepository: FileRepository,
) :
    BaseUseCaseWithResult<OCFile?, ManageDeepLinkUseCase.Params>() {

    override fun run(params: Params): OCFile? {
        val path = params.uri.fragment ?: params.uri.path
        val pathParts = path.split(PATH_SEPARATOR)
        if (pathParts[pathParts.size - 2] != DEEP_LINK_PREVIOUS_PATH_SEGMENT) {
            throw DeepLinkException()
        }

        return fileRepository.getFileFromRemoteId(pathParts[pathParts.size - 1], params.accountName)
    }

    data class Params(val uri: URI, val accountName: String)

    companion object {
        const val DEEP_LINK_PREVIOUS_PATH_SEGMENT = "f"
    }

}
