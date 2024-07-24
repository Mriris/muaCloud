

package com.owncloud.android.data.spaces.repository

import com.owncloud.android.data.spaces.datasources.LocalSpacesDataSource
import com.owncloud.android.data.spaces.datasources.RemoteSpacesDataSource
import com.owncloud.android.data.user.datasources.LocalUserDataSource
import com.owncloud.android.domain.spaces.SpacesRepository
import com.owncloud.android.domain.spaces.model.OCSpace
import com.owncloud.android.domain.user.model.UserQuota

class OCSpacesRepository(
    private val localSpacesDataSource: LocalSpacesDataSource,
    private val localUserDataSource: LocalUserDataSource,
    private val remoteSpacesDataSource: RemoteSpacesDataSource,
) : SpacesRepository {
    override fun refreshSpacesForAccount(accountName: String) {
        remoteSpacesDataSource.refreshSpacesForAccount(accountName).also { listOfSpaces ->
            localSpacesDataSource.saveSpacesForAccount(listOfSpaces)
            val personalSpace = listOfSpaces.find { it.isPersonal }
            personalSpace?.let {
                lateinit var userQuota: UserQuota
                if (it.quota?.total!!.toInt() == 0) {
                    userQuota = UserQuota(accountName, -3, it.quota?.used!!)
                } else {
                    userQuota = UserQuota(accountName, it.quota?.remaining!!, it.quota?.used!!)
                }
                localUserDataSource.saveQuotaForAccount(accountName, userQuota)
            }

        }
    }

    override fun getSpacesFromEveryAccountAsStream() =
        localSpacesDataSource.getSpacesFromEveryAccountAsStream()

    override fun getSpacesByDriveTypeWithSpecialsForAccountAsFlow(accountName: String, filterDriveTypes: Set<String>) =
        localSpacesDataSource.getSpacesByDriveTypeWithSpecialsForAccountAsFlow(accountName = accountName, filterDriveTypes = filterDriveTypes)

    override fun getPersonalSpaceForAccount(accountName: String) =
        localSpacesDataSource.getPersonalSpaceForAccount(accountName)

    override fun getPersonalAndProjectSpacesForAccount(accountName: String) =
        localSpacesDataSource.getPersonalAndProjectSpacesForAccount(accountName)

    override fun getSpaceWithSpecialsByIdForAccount(spaceId: String?, accountName: String) =
        localSpacesDataSource.getSpaceWithSpecialsByIdForAccount(spaceId, accountName)

    override fun getSpaceByIdForAccount(spaceId: String?, accountName: String): OCSpace? =
        localSpacesDataSource.getSpaceByIdForAccount(spaceId = spaceId, accountName = accountName)

    override fun getWebDavUrlForSpace(accountName: String, spaceId: String?): String? =
        localSpacesDataSource.getWebDavUrlForSpace(accountName = accountName, spaceId = spaceId)

}
