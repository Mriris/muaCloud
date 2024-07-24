
package com.owncloud.android.domain.user.usecases

import com.owncloud.android.domain.BaseUseCaseWithResult
import com.owncloud.android.domain.user.UserRepository
import com.owncloud.android.domain.user.model.UserQuota

class GetStoredQuotaUseCase(
    private val userRepository: UserRepository
) : BaseUseCaseWithResult<UserQuota?, GetStoredQuotaUseCase.Params>() {
    override fun run(params: Params): UserQuota? =
        userRepository.getStoredUserQuota(params.accountName)

    data class Params(val accountName: String)
}
