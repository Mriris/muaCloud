

package com.owncloud.android.data.roommigrations

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.filters.SmallTest
import com.owncloud.android.data.ProviderMeta.ProviderTableMeta.CAPABILITIES_ACCOUNT_NAME
import com.owncloud.android.data.ProviderMeta.ProviderTableMeta.CAPABILITIES_CORE_POLLINTERVAL
import com.owncloud.android.data.ProviderMeta.ProviderTableMeta.CAPABILITIES_SHARING_PUBLIC_EXPIRE_DATE_DAYS
import com.owncloud.android.data.ProviderMeta.ProviderTableMeta.CAPABILITIES_TABLE_NAME
import com.owncloud.android.data.ProviderMeta.ProviderTableMeta.LEGACY_CAPABILITIES_VERSION_MAYOR
import com.owncloud.android.data.ProviderMeta.ProviderTableMeta.CAPABILITIES_VERSION_MICRO
import com.owncloud.android.data.ProviderMeta.ProviderTableMeta.CAPABILITIES_VERSION_MINOR
import com.owncloud.android.data.migrations.MIGRATION_27_28
import com.owncloud.android.testutil.OC_CAPABILITY
import org.junit.Assert.assertEquals
import org.junit.Test


@SmallTest
class MigrationToDB28Test : MigrationTest() {

    @Test
    fun migrate27To28() {
        performMigrationTest(
            previousVersion = DB_VERSION_27,
            currentVersion = DB_VERSION_28,
            insertData = { database -> insertDataToTest(database) },
            validateMigration = { database -> validateMigrationTo28(database) },
            listOfMigrations = arrayOf(MIGRATION_27_28)
        )
    }

    @Test
    fun startInVersion28_containsCorrectData() {
        performMigrationTest(
            previousVersion = DB_VERSION_28,
            currentVersion = DB_VERSION_28,
            insertData = { database -> insertDataToTest(database) },
            validateMigration = { database -> validateMigrationTo28(database) },
            listOfMigrations = arrayOf()
        )
    }

    private fun insertDataToTest(database: SupportSQLiteDatabase) {
        database.run {
            insert(
                CAPABILITIES_TABLE_NAME,
                SQLiteDatabase.CONFLICT_NONE,
                cvWithDefaultValues
            )
            close()
        }
    }

    private fun validateMigrationTo28(database: SupportSQLiteDatabase) {
        val count = getCount(database, CAPABILITIES_TABLE_NAME)
        assertEquals(1, count)
        database.close()
    }

    companion object {
        val cvWithDefaultValues = ContentValues().apply {
            put(CAPABILITIES_ACCOUNT_NAME, OC_CAPABILITY.accountName)
            put(LEGACY_CAPABILITIES_VERSION_MAYOR, OC_CAPABILITY.versionMajor)
            put(CAPABILITIES_VERSION_MINOR, OC_CAPABILITY.versionMinor)
            put(CAPABILITIES_VERSION_MICRO, OC_CAPABILITY.versionMicro)
            put(CAPABILITIES_CORE_POLLINTERVAL, OC_CAPABILITY.corePollInterval)
            put(CAPABILITIES_SHARING_PUBLIC_EXPIRE_DATE_DAYS, OC_CAPABILITY.filesSharingPublicExpireDateDays)
        }
    }
}
