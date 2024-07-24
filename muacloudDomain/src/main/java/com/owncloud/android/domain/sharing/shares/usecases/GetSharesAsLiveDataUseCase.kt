

package com.owncloud.android.domain.sharing.shares.usecases

import androidx.lifecycle.LiveData
import com.owncloud.android.domain.BaseUseCase
import com.owncloud.android.domain.sharing.shares.ShareRepository
import com.owncloud.android.domain.sharing.shares.model.OCShare

class GetSharesAsLiveDataUseCase(
    private val shareRepository: ShareRepository
) : BaseUseCase<LiveData<List<OCShare>>, GetSharesAsLiveDataUseCase.Params>() {

    override fun run(params: Params): LiveData<List<OCShare>> = shareRepository.getSharesAsLiveData(
        params.filePath,
        params.accountName
    )

    data class Params(
        val filePath: String,
        val accountName: String
    )
}
