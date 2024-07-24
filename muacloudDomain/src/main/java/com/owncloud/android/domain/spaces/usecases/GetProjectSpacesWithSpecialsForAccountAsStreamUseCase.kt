

package com.owncloud.android.domain.spaces.usecases

import com.owncloud.android.domain.BaseUseCase
import com.owncloud.android.domain.spaces.SpacesRepository
import com.owncloud.android.domain.spaces.model.OCSpace
import com.owncloud.android.domain.spaces.model.OCSpace.Companion.DRIVE_TYPE_PROJECT
import kotlinx.coroutines.flow.Flow

class GetProjectSpacesWithSpecialsForAccountAsStreamUseCase(
    private val spacesRepository: SpacesRepository
) : BaseUseCase<Flow<List<OCSpace>>, GetProjectSpacesWithSpecialsForAccountAsStreamUseCase.Params>() {

    override fun run(params: Params) = spacesRepository.getSpacesByDriveTypeWithSpecialsForAccountAsFlow(
        accountName = params.accountName,
        filterDriveTypes = setOf(DRIVE_TYPE_PROJECT),
    )

    data class Params(
        val accountName: String
    )
}
