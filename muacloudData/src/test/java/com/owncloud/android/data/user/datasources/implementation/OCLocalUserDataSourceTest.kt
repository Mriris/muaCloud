

package com.owncloud.android.data.user.datasources.implementation

import com.owncloud.android.data.user.datasources.implementation.OCLocalUserDataSource.Companion.toEntity
import com.owncloud.android.data.user.datasources.implementation.OCLocalUserDataSource.Companion.toModel
import com.owncloud.android.data.user.db.UserDao
import com.owncloud.android.testutil.OC_ACCOUNT_NAME
import com.owncloud.android.testutil.OC_USER_QUOTA
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class OCLocalUserDataSourceTest {
    private lateinit var ocLocalUserDataSource: OCLocalUserDataSource
    private val ocUserQuotaDao = mockk<UserDao>(relaxUnitFun = true)

    private val userQuotaEntity = OC_USER_QUOTA.toEntity()

    @Before
    fun setUp() {
        ocLocalUserDataSource = OCLocalUserDataSource(ocUserQuotaDao)
    }

    @Test
    fun `saveQuotaForAccount saves user quota correctly`() {

        ocLocalUserDataSource.saveQuotaForAccount(OC_ACCOUNT_NAME, OC_USER_QUOTA)

        verify(exactly = 1) {
            ocUserQuotaDao.insertOrReplace(userQuotaEntity)
        }
    }

    @Test
    fun `getQuotaForAccount returns a UserQuota`() {
        every { ocUserQuotaDao.getQuotaForAccount(any()) } returns userQuotaEntity

        val userQuota = ocLocalUserDataSource.getQuotaForAccount(OC_ACCOUNT_NAME)

        assertEquals(OC_USER_QUOTA, userQuota)

        verify(exactly = 1) {
            ocUserQuotaDao.getQuotaForAccount(OC_ACCOUNT_NAME)
        }
    }

    @Test
    fun `getQuotaForAccount returns null when DAO returns a null quota`() {
        every { ocUserQuotaDao.getQuotaForAccount(any()) } returns null

        val quotaEntity = ocLocalUserDataSource.getQuotaForAccount(OC_ACCOUNT_NAME)

        assertNull(quotaEntity)

        verify(exactly = 1) {
            ocUserQuotaDao.getQuotaForAccount(OC_ACCOUNT_NAME)
        }
    }

    @Test
    fun `deleteQuotaForAccount removes user quota correctly`() {

        ocLocalUserDataSource.deleteQuotaForAccount(OC_ACCOUNT_NAME)

        verify(exactly = 1) {
            ocUserQuotaDao.deleteQuotaForAccount(OC_ACCOUNT_NAME)
        }
    }

    @Test
    fun `getAllUserQuotas returns a list of UserQuota`() {

        every { ocUserQuotaDao.getAllUserQuotas() } returns listOf(userQuotaEntity)

        val resultActual = ocLocalUserDataSource.getAllUserQuotas()

        assertEquals(listOf(userQuotaEntity.toModel()), resultActual)

        verify(exactly = 1) {
            ocUserQuotaDao.getAllUserQuotas()
        }
    }
}
