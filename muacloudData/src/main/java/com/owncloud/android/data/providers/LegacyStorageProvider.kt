
package com.owncloud.android.data.providers

import android.os.Environment
import java.io.File

@Deprecated("Do not use this anymore. We have moved to Scoped Storage")
class LegacyStorageProvider(
    rootFolderName: String
) : LocalStorageProvider(rootFolderName) {

    override fun getPrimaryStorageDirectory(): File = Environment.getExternalStorageDirectory()
}
