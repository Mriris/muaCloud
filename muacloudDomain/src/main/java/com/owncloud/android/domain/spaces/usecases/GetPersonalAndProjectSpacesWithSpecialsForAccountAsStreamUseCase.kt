

package com.owncloud.android.domain.spaces.usecases

import com.owncloud.android.domain.BaseUseCase
import com.owncloud.android.domain.spaces.SpacesRepository
import com.owncloud.android.domain.spaces.model.OCSpace
import kotlinx.coroutines.flow.Flow

class GetPersonalAndProjectSpacesWithSpecialsForAccountAsStreamUseCase(
    private val spacesRepository: SpacesRepository
) : BaseUseCase<Flow<List<OCSpace>>, GetPersonalAndProjectSpacesWithSpecialsForAccountAsStreamUseCase.Params>() {

    override fun run(params: Params) = spacesRepository.getSpacesByDriveTypeWithSpecialsForAccountAsFlow(
        accountName = params.accountName,
        filterDriveTypes = setOf(OCSpace.DRIVE_TYPE_PERSONAL, OCSpace.DRIVE_TYPE_PROJECT),
    )

    data class Params(
        val accountName: String
    )
}
