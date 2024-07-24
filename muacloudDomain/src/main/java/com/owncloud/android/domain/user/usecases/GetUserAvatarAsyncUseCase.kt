
package com.owncloud.android.domain.user.usecases

import com.owncloud.android.domain.BaseUseCaseWithResult
import com.owncloud.android.domain.user.UserRepository
import com.owncloud.android.domain.user.model.UserAvatar

class GetUserAvatarAsyncUseCase(
    private val userRepository: UserRepository
) : BaseUseCaseWithResult<UserAvatar, GetUserAvatarAsyncUseCase.Params>() {
    override fun run(params: Params): UserAvatar =
        userRepository.getUserAvatar(params.accountName)

    data class Params(val accountName: String)
}
