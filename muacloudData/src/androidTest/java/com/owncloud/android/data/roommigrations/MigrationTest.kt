
package com.owncloud.android.data.roommigrations

import androidx.room.migration.Migration
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import com.owncloud.android.data.OwncloudDatabase
import org.junit.Rule

open class MigrationTest {
    @Rule
    @JvmField
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        OwncloudDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    fun getCount(db: SupportSQLiteDatabase, tableName: String): Long =
        db.compileStatement("SELECT COUNT(*) FROM `$tableName`").simpleQueryForLong()

    fun performMigrationTest(
        previousVersion: Int,
        currentVersion: Int,
        insertData: (SupportSQLiteDatabase) -> Unit,
        recoverPreviousData: Boolean = true,
        validateMigration: (SupportSQLiteDatabase) -> Unit,
        listOfMigrations: Array<Migration>
    ) {
        helper.createDatabase(TEST_DB_NAME, previousVersion).run {
            if (recoverPreviousData) insertData(this)
        }

        helper.runMigrationsAndValidate(
            TEST_DB_NAME, currentVersion, true, *listOfMigrations
        ).also { validateMigration(it) }
    }

    companion object {
        const val TEST_DB_NAME = "migration-test"

        const val DB_VERSION_27 = 27
        const val DB_VERSION_28 = 28
        const val DB_VERSION_29 = 29
        const val DB_VERSION_30 = 30
        const val DB_VERSION_31 = 31
        const val DB_VERSION_32 = 32
        const val DB_VERSION_33 = 33
        const val DB_VERSION_34 = 34
        const val DB_VERSION_35 = 35
        const val DB_VERSION_36 = 36

    }
}
