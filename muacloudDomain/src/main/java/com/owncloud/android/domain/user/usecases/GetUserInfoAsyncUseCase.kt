

package com.owncloud.android.domain.user.usecases

import com.owncloud.android.domain.BaseUseCaseWithResult
import com.owncloud.android.domain.user.UserRepository
import com.owncloud.android.domain.user.model.UserInfo

class GetUserInfoAsyncUseCase(
    private val userRepository: UserRepository
) : BaseUseCaseWithResult<UserInfo, GetUserInfoAsyncUseCase.Params>() {
    override fun run(params: Params): UserInfo =
        userRepository.getUserInfo(params.accountName)

    data class Params(val accountName: String)
}
