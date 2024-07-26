

package com.owncloud.android.data

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import com.owncloud.android.data.appregistry.db.AppRegistryDao
import com.owncloud.android.data.appregistry.db.AppRegistryEntity
import com.owncloud.android.data.capabilities.db.OCCapabilityDao
import com.owncloud.android.data.capabilities.db.OCCapabilityEntity
import com.owncloud.android.data.files.db.FileDao
import com.owncloud.android.data.files.db.OCFileEntity
import com.owncloud.android.data.files.db.OCFileSyncEntity
import com.owncloud.android.data.folderbackup.db.FolderBackUpEntity
import com.owncloud.android.data.folderbackup.db.FolderBackupDao
import com.owncloud.android.data.migrations.AutoMigration39To40
import com.owncloud.android.data.migrations.MIGRATION_27_28
import com.owncloud.android.data.migrations.MIGRATION_28_29
import com.owncloud.android.data.migrations.MIGRATION_29_30
import com.owncloud.android.data.migrations.MIGRATION_30_31
import com.owncloud.android.data.migrations.MIGRATION_31_32
import com.owncloud.android.data.migrations.MIGRATION_32_33
import com.owncloud.android.data.migrations.MIGRATION_33_34
import com.owncloud.android.data.migrations.MIGRATION_34_35
import com.owncloud.android.data.migrations.MIGRATION_35_36
import com.owncloud.android.data.migrations.MIGRATION_37_38
import com.owncloud.android.data.migrations.MIGRATION_41_42
import com.owncloud.android.data.migrations.MIGRATION_42_43
import com.owncloud.android.data.sharing.shares.db.OCShareDao
import com.owncloud.android.data.sharing.shares.db.OCShareEntity
import com.owncloud.android.data.spaces.db.SpaceSpecialEntity
import com.owncloud.android.data.spaces.db.SpacesDao
import com.owncloud.android.data.spaces.db.SpacesEntity
import com.owncloud.android.data.transfers.db.OCTransferEntity
import com.owncloud.android.data.transfers.db.TransferDao
import com.owncloud.android.data.user.db.UserDao
import com.owncloud.android.data.user.db.UserQuotaEntity

@Database(
    entities = [
        AppRegistryEntity::class,
        FolderBackUpEntity::class,
        OCCapabilityEntity::class,
        OCFileEntity::class,
        OCFileSyncEntity::class,
        OCShareEntity::class,
        OCTransferEntity::class,
        SpacesEntity::class,
        SpaceSpecialEntity::class,
        UserQuotaEntity::class,
    ],
    autoMigrations = [
        AutoMigration(from = 36, to = 37),
        AutoMigration(from = 38, to = 39),
        AutoMigration(from = 39, to = 40, spec = AutoMigration39To40::class),
        AutoMigration(from = 40, to = 41),
        AutoMigration(from = 43, to = 44),
    ],
    version = ProviderMeta.DB_VERSION,
    exportSchema = true
)
abstract class OwncloudDatabase : RoomDatabase() {
    abstract fun appRegistryDao(): AppRegistryDao
    abstract fun capabilityDao(): OCCapabilityDao
    abstract fun fileDao(): FileDao
    abstract fun folderBackUpDao(): FolderBackupDao
    abstract fun shareDao(): OCShareDao
    abstract fun spacesDao(): SpacesDao
    abstract fun transferDao(): TransferDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: OwncloudDatabase? = null

        val ALL_MIGRATIONS = arrayOf(
            MIGRATION_27_28,
            MIGRATION_28_29,
            MIGRATION_29_30,
            MIGRATION_30_31,
            MIGRATION_31_32,
            MIGRATION_32_33,
            MIGRATION_33_34,
            MIGRATION_34_35,
            MIGRATION_35_36,
            MIGRATION_37_38,
            MIGRATION_41_42,
            MIGRATION_42_43,
        )

        fun getDatabase(
            context: Context
        ): OwncloudDatabase {


            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OwncloudDatabase::class.java,
                    ProviderMeta.NEW_DB_NAME
                )
                    .addMigrations(*ALL_MIGRATIONS)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        @VisibleForTesting
        fun switchToInMemory(context: Context, vararg migrations: Migration) {
            INSTANCE = Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                OwncloudDatabase::class.java
            ).addMigrations(*migrations)
                .build()
        }
    }
}
