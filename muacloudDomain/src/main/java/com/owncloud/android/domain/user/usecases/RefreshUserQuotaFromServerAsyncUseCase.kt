
package com.owncloud.android.domain.user.usecases

import com.owncloud.android.domain.BaseUseCaseWithResult
import com.owncloud.android.domain.user.UserRepository
import com.owncloud.android.domain.user.model.UserQuota

class RefreshUserQuotaFromServerAsyncUseCase(
    private val userRepository: UserRepository
) : BaseUseCaseWithResult<UserQuota, RefreshUserQuotaFromServerAsyncUseCase.Params>() {
    override fun run(params: Params): UserQuota =
        userRepository.getUserQuota(params.accountName)

    data class Params(val accountName: String)
}
