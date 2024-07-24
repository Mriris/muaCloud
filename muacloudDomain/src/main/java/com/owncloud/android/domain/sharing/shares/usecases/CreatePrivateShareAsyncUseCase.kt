

package com.owncloud.android.domain.sharing.shares.usecases

import com.owncloud.android.domain.sharing.shares.ShareRepository
import com.owncloud.android.domain.BaseUseCaseWithResult
import com.owncloud.android.domain.sharing.shares.model.ShareType

class CreatePrivateShareAsyncUseCase(
    private val shareRepository: ShareRepository
) : BaseUseCaseWithResult<Unit, CreatePrivateShareAsyncUseCase.Params>() {

    override fun run(params: Params) {
        require(
            params.shareType != null &&
                    (params.shareType == ShareType.USER ||
                            params.shareType == ShareType.GROUP ||
                            params.shareType == ShareType.FEDERATED)
        ) {
            "Illegal share type ${params.shareType}"
        }

        shareRepository.insertPrivateShare(
            params.filePath,
            params.shareType,
            params.shareeName,
            params.permissions,
            params.accountName
        )
    }

    data class Params(
        val filePath: String,
        val shareType: ShareType?,
        val shareeName: String,
        val permissions: Int,
        val accountName: String
    )
}
