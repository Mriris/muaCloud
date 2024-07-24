

package com.owncloud.android.domain.sharing.shares.usecases

import androidx.lifecycle.LiveData
import com.owncloud.android.domain.BaseUseCase
import com.owncloud.android.domain.sharing.shares.ShareRepository
import com.owncloud.android.domain.sharing.shares.model.OCShare

class GetShareAsLiveDataUseCase(
    private val shareRepository: ShareRepository
) : BaseUseCase<LiveData<OCShare>, GetShareAsLiveDataUseCase.Params>() {
    override fun run(params: Params): LiveData<OCShare> =
        shareRepository.getShareAsLiveData(
            params.remoteId
        )

    data class Params(
        val remoteId: String
    )
}
