
package com.owncloud.android.data.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.owncloud.android.data.ProviderMeta.ProviderTableMeta.FOLDER_BACKUP_TABLE_NAME
import com.owncloud.android.data.providers.SharedPreferencesProvider
import com.owncloud.android.domain.camerauploads.model.FolderBackUpConfiguration
import com.owncloud.android.domain.camerauploads.model.FolderBackUpConfiguration.Companion.pictureUploadsName
import com.owncloud.android.domain.camerauploads.model.FolderBackUpConfiguration.Companion.videoUploadsName
import com.owncloud.android.domain.camerauploads.model.UploadBehavior
import java.io.File

val MIGRATION_33_34 = object : Migration(33, 34) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS `$FOLDER_BACKUP_TABLE_NAME` (`accountName` TEXT NOT NULL, `behavior` TEXT NOT NULL, `sourcePath` TEXT NOT NULL, `uploadPath` TEXT NOT NULL, `wifiOnly` INTEGER NOT NULL, `name` TEXT NOT NULL, `lastSyncTimestamp` INTEGER NOT NULL, `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL)")
    }
}

@Deprecated("Legacy code. Only used to migrate old camera uploads configuration from ")
class CameraUploadsMigrationToRoom(val sharedPreferencesProvider: SharedPreferencesProvider) {

    fun getPictureUploadsConfigurationPreferences(timestamp: Long): FolderBackUpConfiguration? {

        if (!sharedPreferencesProvider.getBoolean(PREF__CAMERA_PICTURE_UPLOADS_ENABLED, false)) return null

        return FolderBackUpConfiguration(
            accountName = sharedPreferencesProvider.getString(PREF__CAMERA_PICTURE_UPLOADS_ACCOUNT_NAME, null) ?: "",
            wifiOnly = sharedPreferencesProvider.getBoolean(PREF__CAMERA_PICTURE_UPLOADS_WIFI_ONLY, false),
            uploadPath = getUploadPathForPreference(PREF__CAMERA_PICTURE_UPLOADS_PATH),
            sourcePath = getSourcePathForPreference(PREF__CAMERA_PICTURE_UPLOADS_SOURCE),
            behavior = getBehaviorForPreference(PREF__CAMERA_PICTURE_UPLOADS_BEHAVIOUR),
            lastSyncTimestamp = timestamp,
            name = pictureUploadsName,
            chargingOnly = false,
            spaceId = null,
        )
    }

    fun getVideoUploadsConfigurationPreferences(timestamp: Long): FolderBackUpConfiguration? {
        if (!sharedPreferencesProvider.getBoolean(PREF__CAMERA_VIDEO_UPLOADS_ENABLED, false)) return null

        return FolderBackUpConfiguration(
            accountName = sharedPreferencesProvider.getString(PREF__CAMERA_VIDEO_UPLOADS_ACCOUNT_NAME, null) ?: "",
            wifiOnly = sharedPreferencesProvider.getBoolean(PREF__CAMERA_VIDEO_UPLOADS_WIFI_ONLY, false),
            uploadPath = getUploadPathForPreference(PREF__CAMERA_VIDEO_UPLOADS_PATH),
            sourcePath = getSourcePathForPreference(PREF__CAMERA_VIDEO_UPLOADS_SOURCE),
            behavior = getBehaviorForPreference(PREF__CAMERA_VIDEO_UPLOADS_BEHAVIOUR),
            lastSyncTimestamp = timestamp,
            name = videoUploadsName,
            chargingOnly = false,
            spaceId = null,
        )
    }

    private fun getUploadPathForPreference(keyPreference: String): String {
        val uploadPath = sharedPreferencesProvider.getString(
            key = keyPreference,
            defaultValue = DEFAULT_PATH_FOR_CAMERA_UPLOADS + File.separator
        )
        return if (uploadPath!!.endsWith(File.separator)) uploadPath else uploadPath + File.separator
    }

    private fun getSourcePathForPreference(keyPreference: String): String {
        return sharedPreferencesProvider.getString(keyPreference, null) ?: ""
    }

    private fun getBehaviorForPreference(keyPreference: String): UploadBehavior {
        val storedBehaviour = sharedPreferencesProvider.getString(keyPreference, null) ?: return UploadBehavior.COPY

        return UploadBehavior.fromString(storedBehaviour)
    }

    companion object {
        private const val PREF__CAMERA_PICTURE_UPLOADS_ENABLED = "enable_picture_uploads"
        private const val PREF__CAMERA_VIDEO_UPLOADS_ENABLED = "enable_video_uploads"
        private const val PREF__CAMERA_PICTURE_UPLOADS_WIFI_ONLY = "picture_uploads_on_wifi"
        private const val PREF__CAMERA_VIDEO_UPLOADS_WIFI_ONLY = "video_uploads_on_wifi"
        private const val PREF__CAMERA_PICTURE_UPLOADS_PATH = "picture_uploads_path"
        private const val PREF__CAMERA_VIDEO_UPLOADS_PATH = "video_uploads_path"
        private const val PREF__CAMERA_PICTURE_UPLOADS_BEHAVIOUR = "picture_uploads_behaviour"
        private const val PREF__CAMERA_PICTURE_UPLOADS_SOURCE = "picture_uploads_source_path"
        private const val PREF__CAMERA_VIDEO_UPLOADS_BEHAVIOUR = "video_uploads_behaviour"
        private const val PREF__CAMERA_VIDEO_UPLOADS_SOURCE = "video_uploads_source_path"
        private const val PREF__CAMERA_PICTURE_UPLOADS_ACCOUNT_NAME = "picture_uploads_account_name"
        private const val PREF__CAMERA_VIDEO_UPLOADS_ACCOUNT_NAME = "video_uploads_account_name"

        private const val DEFAULT_PATH_FOR_CAMERA_UPLOADS = "/CameraUpload"
    }
}
