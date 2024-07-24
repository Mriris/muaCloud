

package com.owncloud.android.data.folderbackup.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.owncloud.android.data.ProviderMeta
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderBackupDao {
    @Query(SELECT)
    fun getFolderBackUpConfigurationByName(
        name: String
    ): FolderBackUpEntity?

    @Query(SELECT)
    fun getFolderBackUpConfigurationByNameAsFlow(
        name: String
    ): Flow<FolderBackUpEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplace(folderBackUpEntity: FolderBackUpEntity): Long

    @Query(DELETE)
    fun delete(name: String): Int

    @Transaction
    fun update(folderBackUpEntity: FolderBackUpEntity): Long {
        delete(folderBackUpEntity.name)
        return insertOrReplace(folderBackUpEntity)
    }

    companion object {
        private const val SELECT = """
            SELECT *
            FROM ${ProviderMeta.ProviderTableMeta.FOLDER_BACKUP_TABLE_NAME}
            WHERE name = :name
        """

        private const val DELETE = """
            DELETE
            FROM ${ProviderMeta.ProviderTableMeta.FOLDER_BACKUP_TABLE_NAME}
            WHERE name = :name
        """
    }
}
