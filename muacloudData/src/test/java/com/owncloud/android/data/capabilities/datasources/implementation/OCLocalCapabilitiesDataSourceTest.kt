

package com.owncloud.android.data.capabilities.datasources.implementation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.owncloud.android.data.capabilities.datasources.implementation.OCLocalCapabilitiesDataSource.Companion.toEntity
import com.owncloud.android.data.capabilities.db.OCCapabilityDao
import com.owncloud.android.data.capabilities.db.OCCapabilityEntity
import com.owncloud.android.testutil.OC_ACCOUNT_NAME
import com.owncloud.android.testutil.OC_CAPABILITY
import com.owncloud.android.testutil.livedata.getLastEmittedValue
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class OCLocalCapabilitiesDataSourceTest {

    private lateinit var ocLocalCapabilitiesDataSource: OCLocalCapabilitiesDataSource
    private val ocCapabilityDao = mockk<OCCapabilityDao>(relaxUnitFun = true)

    private val ocCapability = OC_CAPABILITY.copy(id = 0)
    private val ocCapabilityEntity = ocCapability.toEntity()

    @Rule
    @JvmField
    var rule: TestRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        ocLocalCapabilitiesDataSource =
            OCLocalCapabilitiesDataSource(
                ocCapabilityDao,
            )
    }

    @Test
    fun `getCapabilitiesForAccountAsLiveData returns a LiveData of OCCapability`() {
        val capabilitiesLiveData = MutableLiveData(ocCapabilityEntity)
        every { ocCapabilityDao.getCapabilitiesForAccountAsLiveData(OC_ACCOUNT_NAME) } returns capabilitiesLiveData

        val result = ocLocalCapabilitiesDataSource.getCapabilitiesForAccountAsLiveData(OC_ACCOUNT_NAME).getLastEmittedValue()

        assertEquals(ocCapability, result)

        verify(exactly = 1) {
            ocCapabilityDao.getCapabilitiesForAccountAsLiveData(OC_ACCOUNT_NAME)
        }
    }

    @Test
    fun `getCapabilitiesForAccountAsLiveData returns null when DAO returns a null capability`() {
        val capabilitiesLiveData = MutableLiveData<OCCapabilityEntity>(null)
        every { ocCapabilityDao.getCapabilitiesForAccountAsLiveData(OC_ACCOUNT_NAME) } returns capabilitiesLiveData

        val result = ocLocalCapabilitiesDataSource.getCapabilitiesForAccountAsLiveData(OC_ACCOUNT_NAME).getLastEmittedValue()

        assertNull(result)

        verify(exactly = 1) {
            ocCapabilityDao.getCapabilitiesForAccountAsLiveData(OC_ACCOUNT_NAME)
        }
    }

    @Test
    fun `getCapabilitiesForAccount returns a OCCapability`() {
        every { ocCapabilityDao.getCapabilitiesForAccount(OC_ACCOUNT_NAME) } returns ocCapabilityEntity

        val result = ocLocalCapabilitiesDataSource.getCapabilitiesForAccount(OC_ACCOUNT_NAME)

        assertEquals(ocCapability, result)

        verify(exactly = 1) {
            ocCapabilityDao.getCapabilitiesForAccount(OC_ACCOUNT_NAME)
        }
    }

    @Test
    fun `getCapabilitiesForAccount returns null when DAO returns a null capability`() {
        every { ocCapabilityDao.getCapabilitiesForAccount(OC_ACCOUNT_NAME) } returns null

        val result = ocLocalCapabilitiesDataSource.getCapabilitiesForAccount(OC_ACCOUNT_NAME)

        assertNull(result)

        verify(exactly = 1) {
            ocCapabilityDao.getCapabilitiesForAccount(OC_ACCOUNT_NAME)
        }
    }

    @Test
    fun `insertCapabilities saves a list of OCCapability correctly`() {
        ocLocalCapabilitiesDataSource.insertCapabilities(listOf(ocCapability))

        verify(exactly = 1) { ocCapabilityDao.replace(listOf(ocCapabilityEntity)) }
    }

    @Test
    fun `deleteCapabilitiesForAccount removes capabilities correctly`() {
        ocLocalCapabilitiesDataSource.deleteCapabilitiesForAccount(OC_ACCOUNT_NAME)

        verify(exactly = 1) { ocCapabilityDao.deleteByAccountName(OC_ACCOUNT_NAME) }
    }
}
