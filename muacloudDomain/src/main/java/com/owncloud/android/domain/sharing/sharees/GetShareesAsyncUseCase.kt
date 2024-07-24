

package com.owncloud.android.domain.sharing.sharees

import com.owncloud.android.domain.BaseUseCaseWithResult
import com.owncloud.android.domain.sharing.sharees.model.OCSharee

class GetShareesAsyncUseCase(
    private val shareeRepository: ShareeRepository
) : BaseUseCaseWithResult<List<OCSharee>, GetShareesAsyncUseCase.Params>() {
    override fun run(params: Params): List<OCSharee> =
        shareeRepository.getSharees(
            searchString = params.searchString,
            page = params.page,
            perPage = params.perPage,
            accountName = params.accountName,
        )

    data class Params(
        val searchString: String,
        val page: Int,
        val perPage: Int,
        val accountName: String,
    )
}
