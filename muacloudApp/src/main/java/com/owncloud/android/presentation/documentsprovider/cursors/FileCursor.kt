

package com.owncloud.android.presentation.documentsprovider.cursors

import android.database.MatrixCursor
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.DocumentsContract.Document
import com.owncloud.android.domain.files.model.OCFile
import com.owncloud.android.utils.MimetypeIconUtil

class FileCursor(projection: Array<String>?) : MatrixCursor(projection ?: DEFAULT_DOCUMENT_PROJECTION) {
    private var cursorExtras = Bundle.EMPTY

    override fun getExtras(): Bundle = cursorExtras

    fun setMoreToSync(hasMoreToSync: Boolean) {
        cursorExtras = Bundle().apply { putBoolean(DocumentsContract.EXTRA_LOADING, hasMoreToSync) }
    }

    fun addFile(file: OCFile) {
        val iconRes = MimetypeIconUtil.getFileTypeIconId(file.mimeType, file.fileName)
        val mimeType = if (file.isFolder) Document.MIME_TYPE_DIR else file.mimeType
        val imagePath = if (file.isImage && file.isAvailableLocally) file.storagePath else null
        var flags = if (imagePath != null) Document.FLAG_SUPPORTS_THUMBNAIL else 0

        flags = flags or Document.FLAG_SUPPORTS_DELETE
        flags = flags or Document.FLAG_SUPPORTS_RENAME
        flags = flags or Document.FLAG_SUPPORTS_COPY
        flags = flags or Document.FLAG_SUPPORTS_MOVE

        if (mimeType != Document.MIME_TYPE_DIR) { // If it is a file
            flags = flags or Document.FLAG_SUPPORTS_WRITE
        } else if (file.hasAddFilePermission && file.hasAddSubdirectoriesPermission) { // If it is a folder with writing permissions
            flags = flags or Document.FLAG_DIR_SUPPORTS_CREATE
        }

        newRow()
            .add(Document.COLUMN_DOCUMENT_ID, file.id.toString())
            .add(Document.COLUMN_DISPLAY_NAME, file.fileName)
            .add(Document.COLUMN_LAST_MODIFIED, file.modificationTimestamp)
            .add(Document.COLUMN_SIZE, file.length)
            .add(Document.COLUMN_FLAGS, flags)
            .add(Document.COLUMN_ICON, iconRes)
            .add(Document.COLUMN_MIME_TYPE, mimeType)
    }

    companion object {
        val DEFAULT_DOCUMENT_PROJECTION = arrayOf(
            Document.COLUMN_DOCUMENT_ID,
            Document.COLUMN_DISPLAY_NAME,
            Document.COLUMN_MIME_TYPE,
            Document.COLUMN_SIZE,
            Document.COLUMN_FLAGS,
            Document.COLUMN_LAST_MODIFIED
        )

    }
}
