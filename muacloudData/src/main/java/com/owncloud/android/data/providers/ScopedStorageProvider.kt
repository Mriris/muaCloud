
package com.owncloud.android.data.providers

import android.content.Context
import java.io.File

class ScopedStorageProvider(
    rootFolderName: String,
    private val context: Context
) : LocalStorageProvider(rootFolderName) {

    override fun getPrimaryStorageDirectory(): File = context.filesDir
}
