

package com.owncloud.android.data.spaces.datasources

import com.owncloud.android.domain.spaces.model.OCSpace
import kotlinx.coroutines.flow.Flow

interface LocalSpacesDataSource {
    fun saveSpacesForAccount(listOfSpaces: List<OCSpace>)
    fun getPersonalSpaceForAccount(accountName: String): OCSpace?
    fun getSharesSpaceForAccount(accountName: String): OCSpace?
    fun getSpacesFromEveryAccountAsStream(): Flow<List<OCSpace>>
    fun getSpacesByDriveTypeWithSpecialsForAccountAsFlow(accountName: String, filterDriveTypes: Set<String>): Flow<List<OCSpace>>
    fun getPersonalAndProjectSpacesForAccount(accountName: String): List<OCSpace>
    fun getSpaceWithSpecialsByIdForAccount(spaceId: String?, accountName: String): OCSpace
    fun getSpaceByIdForAccount(spaceId: String?, accountName: String): OCSpace?
    fun getWebDavUrlForSpace(spaceId: String?, accountName: String): String?
    fun deleteSpacesForAccount(accountName: String)
}
