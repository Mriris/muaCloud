

package com.owncloud.android.data.migrations

import androidx.room.DeleteColumn
import androidx.room.RenameColumn
import androidx.room.migration.AutoMigrationSpec
import com.owncloud.android.data.ProviderMeta.ProviderTableMeta.CAPABILITIES_TABLE_NAME
import com.owncloud.android.data.ProviderMeta.ProviderTableMeta.CAPABILITIES_VERSION_MAJOR
import com.owncloud.android.data.ProviderMeta.ProviderTableMeta.FILES_TABLE_NAME

@RenameColumn(
    tableName = CAPABILITIES_TABLE_NAME,
    fromColumnName = "version_mayor",
    toColumnName = CAPABILITIES_VERSION_MAJOR
)
@RenameColumn(
    tableName = CAPABILITIES_TABLE_NAME,
    fromColumnName = "enabled",
    toColumnName = "enabledAppProviders"
)
@RenameColumn(
    tableName = CAPABILITIES_TABLE_NAME,
    fromColumnName = "version",
    toColumnName = "versionAppProviders"
)
@RenameColumn(
    tableName = CAPABILITIES_TABLE_NAME,
    fromColumnName = "appsUrl",
    toColumnName = "appsUrlAppProviders"
)
@RenameColumn(
    tableName = CAPABILITIES_TABLE_NAME,
    fromColumnName = "openUrl",
    toColumnName = "openUrlAppProviders"
)
@RenameColumn(
    tableName = CAPABILITIES_TABLE_NAME,
    fromColumnName = "openWebUrl",
    toColumnName = "openWebUrlAppProviders"
)
@RenameColumn(
    tableName = CAPABILITIES_TABLE_NAME,
    fromColumnName = "newUrl",
    toColumnName = "newUrlAppProviders"
)
@DeleteColumn(
    tableName = FILES_TABLE_NAME,
    columnName = "lastSyncDateForProperties"
)
class AutoMigration39To40 : AutoMigrationSpec
