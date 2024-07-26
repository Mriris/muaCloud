
package com.owncloud.android.data.user

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import com.owncloud.android.data.OwncloudDatabase
import com.owncloud.android.data.user.datasources.implementation.OCLocalUserDataSource.Companion.toEntity
import com.owncloud.android.data.user.datasources.implementation.OCLocalUserDataSource.Companion.toModel
import com.owncloud.android.data.user.db.UserDao
import com.owncloud.android.testutil.OC_ACCOUNT_NAME
import com.owncloud.android.testutil.OC_USER_QUOTA
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@MediumTest
class UserDaoTest {

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var userDao: UserDao
    private val userQuotaEntity = OC_USER_QUOTA.toEntity()

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        OwncloudDatabase.switchToInMemory(context)
        val db: OwncloudDatabase = OwncloudDatabase.getDatabase(context)
        userDao = db.userDao()
    }


    @Test
    fun insertQuotaForAccount() {
        userDao.insertOrReplace(userQuotaEntity)

        val userQuotaEntity = userDao.getQuotaForAccount(OC_ACCOUNT_NAME)

        assertNotNull(userQuotaEntity)
        assertEquals(OC_USER_QUOTA, userQuotaEntity?.toModel())
    }

    @Test
    fun replaceQuotaForAccount() {
        userDao.insertOrReplace(userQuotaEntity)
        userDao.insertOrReplace(userQuotaEntity.copy(available = -3))

        val userQuotaEntity = userDao.getQuotaForAccount(OC_ACCOUNT_NAME)

        assertNotNull(userQuotaEntity)
        assertEquals(OC_USER_QUOTA.copy(available = -3), userQuotaEntity?.toModel())
    }

    @Test
    fun getQuotaForAccountNull() {
        val userQuotaEntity = userDao.getQuotaForAccount(OC_ACCOUNT_NAME)

        assertNull(userQuotaEntity)
    }
}
