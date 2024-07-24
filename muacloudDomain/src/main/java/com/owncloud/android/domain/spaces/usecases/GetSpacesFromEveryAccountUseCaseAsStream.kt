

package com.owncloud.android.domain.spaces.usecases

import com.owncloud.android.domain.BaseUseCase
import com.owncloud.android.domain.spaces.SpacesRepository
import com.owncloud.android.domain.spaces.model.OCSpace
import kotlinx.coroutines.flow.Flow

class GetSpacesFromEveryAccountUseCaseAsStream(
    private val spacesRepository: SpacesRepository
) : BaseUseCase<Flow<List<OCSpace>>, Unit>() {
    override fun run(params: Unit) =
        spacesRepository.getSpacesFromEveryAccountAsStream()
}
