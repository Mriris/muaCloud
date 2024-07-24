

package com.owncloud.android.presentation.documentsprovider.cursors

import android.accounts.Account
import android.content.Context
import android.database.MatrixCursor
import android.provider.DocumentsContract.Root
import com.owncloud.android.R
import com.owncloud.android.datamodel.FileDataStorageManager

class RootCursor(projection: Array<String>?) : MatrixCursor(projection ?: DEFAULT_ROOT_PROJECTION) {

    fun addRoot(account: Account, context: Context, spacesAllowed: Boolean) {
        val manager = FileDataStorageManager(account)
        val mainDirId = if (spacesAllowed) {
            // To display the list of spaces for an account, we need to do this trick.
            // If the document id is not a number, we will know that it is the time to display the list of spaces for the account
            account.name
        } else {
            // Root directory of the personal space (oCIS) or "Files" (oC10)
            manager.getRootPersonalFolder()?.id
        }

        val flags = Root.FLAG_SUPPORTS_SEARCH or Root.FLAG_SUPPORTS_CREATE

        newRow()
            .add(Root.COLUMN_ROOT_ID, account.name)
            .add(Root.COLUMN_DOCUMENT_ID, mainDirId)
            .add(Root.COLUMN_SUMMARY, account.name)
            .add(Root.COLUMN_TITLE, context.getString(R.string.app_name))
            .add(Root.COLUMN_ICON, R.mipmap.icon)
            .add(Root.COLUMN_FLAGS, flags)
    }

    fun addProtectedRoot(context: Context) {
        newRow()
            .add(
                Root.COLUMN_SUMMARY,
                context.getString(R.string.document_provider_locked)
            )
            .add(Root.COLUMN_TITLE, context.getString(R.string.app_name))
            .add(Root.COLUMN_ICON, R.mipmap.icon)
    }

    companion object {
        private val DEFAULT_ROOT_PROJECTION = arrayOf(
            Root.COLUMN_ROOT_ID,
            Root.COLUMN_FLAGS,
            Root.COLUMN_ICON,
            Root.COLUMN_TITLE,
            Root.COLUMN_DOCUMENT_ID,
            Root.COLUMN_AVAILABLE_BYTES,
            Root.COLUMN_SUMMARY,
            Root.COLUMN_FLAGS
        )
    }
}
