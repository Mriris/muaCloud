

package com.owncloud.android.data.folderbackup.datasources.implementation

import com.owncloud.android.data.folderbackup.datasources.implementation.OCLocalFolderBackupDataSource.Companion.toModel
import com.owncloud.android.data.folderbackup.db.FolderBackupDao
import com.owncloud.android.domain.camerauploads.model.FolderBackUpConfiguration
import com.owncloud.android.testutil.OC_BACKUP
import com.owncloud.android.testutil.OC_BACKUP_ENTITY
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class OCLocalFolderBackupDataSourceTest {

    private lateinit var ocLocalFolderBackupDataSource: OCLocalFolderBackupDataSource
    private val folderBackupDao = mockk<FolderBackupDao>(relaxed = true)

    @Before
    fun setUp() {
        ocLocalFolderBackupDataSource = OCLocalFolderBackupDataSource(folderBackupDao)
    }

    @Test
    fun `getCameraUploadsConfiguration returns a CameraUploadsConfiguration when having valid configurations`() {
        every { folderBackupDao.getFolderBackUpConfigurationByName(FolderBackUpConfiguration.pictureUploadsName) } returns OC_BACKUP_ENTITY
        every { folderBackupDao.getFolderBackUpConfigurationByName(FolderBackUpConfiguration.videoUploadsName) } returns OC_BACKUP_ENTITY

        val resultCurrent = ocLocalFolderBackupDataSource.getCameraUploadsConfiguration()

        assertEquals(OC_BACKUP_ENTITY.toModel(), resultCurrent?.pictureUploadsConfiguration)
        assertEquals(OC_BACKUP_ENTITY.toModel(), resultCurrent?.videoUploadsConfiguration)

        verify(exactly = 1) {
            folderBackupDao.getFolderBackUpConfigurationByName(FolderBackUpConfiguration.pictureUploadsName)
            folderBackupDao.getFolderBackUpConfigurationByName(FolderBackUpConfiguration.videoUploadsName)
        }
    }

    @Test
    fun `getCameraUploadsConfiguration returns null when there are not configurations`() {
        every { folderBackupDao.getFolderBackUpConfigurationByName(FolderBackUpConfiguration.pictureUploadsName) } returns null
        every { folderBackupDao.getFolderBackUpConfigurationByName(FolderBackUpConfiguration.videoUploadsName) } returns null

        val resultCurrent = ocLocalFolderBackupDataSource.getCameraUploadsConfiguration()

        assertNull(resultCurrent)

        verify(exactly = 1) {
            folderBackupDao.getFolderBackUpConfigurationByName(FolderBackUpConfiguration.pictureUploadsName)
            folderBackupDao.getFolderBackUpConfigurationByName(FolderBackUpConfiguration.videoUploadsName)
        }
    }

    @Test
    fun `getFolderBackupConfigurationByNameAsFlow returns a Flow of CameraUploadsConfiguration when having valid configurations`() = runBlocking {
        every { folderBackupDao.getFolderBackUpConfigurationByNameAsFlow(FolderBackUpConfiguration.pictureUploadsName) } returns flowOf(
            OC_BACKUP_ENTITY
        )

        val resultCurrent = ocLocalFolderBackupDataSource.getFolderBackupConfigurationByNameAsFlow(FolderBackUpConfiguration.pictureUploadsName)

        val result = resultCurrent.first()
        assertEquals(OC_BACKUP_ENTITY.toModel(), result)

        verify(exactly = 1) {
            folderBackupDao.getFolderBackUpConfigurationByNameAsFlow(FolderBackUpConfiguration.pictureUploadsName)
        }
    }

    @Test
    fun `saveFolderBackupConfiguration saves valid configurations correctly`() {
        ocLocalFolderBackupDataSource.saveFolderBackupConfiguration(OC_BACKUP)

        verify(exactly = 1) {
            folderBackupDao.update(OC_BACKUP_ENTITY)
        }
    }

    @Test
    fun `resetFolderBackupConfigurationByName removes current folder backup configuration correctly`() {
        ocLocalFolderBackupDataSource.resetFolderBackupConfigurationByName(FolderBackUpConfiguration.pictureUploadsName)

        verify(exactly = 1) {
            folderBackupDao.delete(FolderBackUpConfiguration.pictureUploadsName)
        }
    }
}
