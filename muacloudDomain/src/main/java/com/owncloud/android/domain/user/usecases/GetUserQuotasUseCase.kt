

package com.owncloud.android.domain.user.usecases

import com.owncloud.android.domain.BaseUseCase
import com.owncloud.android.domain.user.UserRepository
import com.owncloud.android.domain.user.model.UserQuota

class GetUserQuotasUseCase(
    private val userRepository: UserRepository
) : BaseUseCase<List<UserQuota>, Unit>() {
    override fun run(params: Unit): List<UserQuota> =
        userRepository.getAllUserQuotas()

}
