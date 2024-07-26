
package com.owncloud.android.presentation.documentsprovider

import android.content.Context
import android.provider.DocumentsContract
import com.owncloud.android.R

class DocumentsProviderUtils {
    companion object {

        fun notifyDocumentsProviderRoots(context: Context) {
            val authority = context.resources.getString(R.string.document_provider_authority)
            val rootsUri = DocumentsContract.buildRootsUri(authority)
            context.contentResolver.notifyChange(rootsUri, null)
        }
    }
}
