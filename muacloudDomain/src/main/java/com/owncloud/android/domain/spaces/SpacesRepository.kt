

package com.owncloud.android.domain.spaces

import com.owncloud.android.domain.spaces.model.OCSpace
import kotlinx.coroutines.flow.Flow

interface SpacesRepository {
    fun refreshSpacesForAccount(accountName: String)
    fun getSpacesFromEveryAccountAsStream(): Flow<List<OCSpace>>
    fun getSpacesByDriveTypeWithSpecialsForAccountAsFlow(accountName: String, filterDriveTypes: Set<String>): Flow<List<OCSpace>>
    fun getPersonalSpaceForAccount(accountName: String): OCSpace?
    fun getPersonalAndProjectSpacesForAccount(accountName: String): List<OCSpace>
    fun getSpaceWithSpecialsByIdForAccount(spaceId: String?, accountName: String): OCSpace
    fun getSpaceByIdForAccount(spaceId: String?, accountName: String): OCSpace?
    fun getWebDavUrlForSpace(accountName: String, spaceId: String?): String?
}
