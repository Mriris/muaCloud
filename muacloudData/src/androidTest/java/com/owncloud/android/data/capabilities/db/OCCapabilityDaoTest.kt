

package com.owncloud.android.data.capabilities.db

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import com.owncloud.android.data.OwncloudDatabase
import com.owncloud.android.data.capabilities.datasources.implementation.OCLocalCapabilitiesDataSource.Companion.toEntity
import com.owncloud.android.domain.capabilities.model.CapabilityBooleanType
import com.owncloud.android.testutil.OC_CAPABILITY
import com.owncloud.android.testutil.livedata.getLastEmittedValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@SmallTest
class OCCapabilityDaoTest {
    private lateinit var ocCapabilityDao: OCCapabilityDao
    private val user1 = "user1@server"
    private val user2 = "user2@server"

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        OwncloudDatabase.switchToInMemory(context)
        val db: OwncloudDatabase = OwncloudDatabase.getDatabase(context)
        ocCapabilityDao = db.capabilityDao()
    }

    @Test
    fun insertCapabilitiesListAndRead() {
        val entityList: List<OCCapabilityEntity> = listOf(
            OC_CAPABILITY.copy(accountName = user1).toEntity(),
            OC_CAPABILITY.copy(accountName = user2).toEntity()
        )

        ocCapabilityDao.insertOrReplace(entityList)

        val capability = ocCapabilityDao.getCapabilitiesForAccount(user2)
        val capabilityAsLiveData = ocCapabilityDao.getCapabilitiesForAccountAsLiveData(user2).getLastEmittedValue()

        assertNotNull(capability)
        assertNotNull(capabilityAsLiveData)
        assertEquals(entityList[1], capability)
        assertEquals(entityList[1], capabilityAsLiveData)
    }

    @Test
    fun insertCapabilitiesAndRead() {
        val entity1 = OC_CAPABILITY.copy(accountName = user1).toEntity()
        val entity2 = OC_CAPABILITY.copy(accountName = user2).toEntity()

        ocCapabilityDao.insertOrReplace(entity1)
        ocCapabilityDao.insertOrReplace(entity2)

        val capability = ocCapabilityDao.getCapabilitiesForAccount(user2)
        val capabilityAsLiveData = ocCapabilityDao.getCapabilitiesForAccountAsLiveData(user2).getLastEmittedValue()

        assertNotNull(capability)
        assertNotNull(capabilityAsLiveData)
        assertEquals(entity2, capability)
        assertEquals(entity2, capabilityAsLiveData)
    }

    @Test
    fun getNonExistingCapabilities() {
        ocCapabilityDao.insertOrReplace(OC_CAPABILITY.copy(accountName = user1).toEntity())

        val capability = ocCapabilityDao.getCapabilitiesForAccountAsLiveData(user2).getLastEmittedValue()

        assertNull(capability)
    }

    @Test
    fun replaceCapabilityIfAlreadyExists_exists() {
        val entity1 = OC_CAPABILITY.copy(filesVersioning = CapabilityBooleanType.FALSE).toEntity()
        val entity2 = OC_CAPABILITY.copy(filesVersioning = CapabilityBooleanType.TRUE).toEntity()

        ocCapabilityDao.insertOrReplace(entity1)
        ocCapabilityDao.replace(listOf(entity2))

        val capability = ocCapabilityDao.getCapabilitiesForAccountAsLiveData(OC_CAPABILITY.accountName!!).getLastEmittedValue()

        assertNotNull(capability)
        assertEquals(entity2, capability)
    }

    @Test
    fun replaceCapabilityIfAlreadyExists_doesNotExist() {
        val entity1 = OC_CAPABILITY.copy(accountName = user1).toEntity()
        val entity2 = OC_CAPABILITY.copy(accountName = user2).toEntity()

        ocCapabilityDao.insertOrReplace(entity1)

        ocCapabilityDao.replace(listOf(entity2))

        val capability1 = ocCapabilityDao.getCapabilitiesForAccountAsLiveData(user1).getLastEmittedValue()

        assertNotNull(capability1)
        assertEquals(entity1, capability1)

        val capability2 = ocCapabilityDao.getCapabilitiesForAccountAsLiveData(user2).getLastEmittedValue()

        assertNotNull(capability2)
        assertEquals(entity2, capability2)
    }

    @Test
    fun deleteCapability() {
        val entity = OC_CAPABILITY.copy(accountName = user1).toEntity()

        ocCapabilityDao.insertOrReplace(entity)

        val capability1 = ocCapabilityDao.getCapabilitiesForAccountAsLiveData(user1).getLastEmittedValue()

        assertNotNull(capability1)

        ocCapabilityDao.deleteByAccountName(user1)

        val capability2 = ocCapabilityDao.getCapabilitiesForAccountAsLiveData(user1).getLastEmittedValue()

        assertNull(capability2)
    }
}
