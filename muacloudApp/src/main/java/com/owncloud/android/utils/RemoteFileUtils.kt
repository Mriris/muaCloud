

package com.owncloud.android.utils

import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.resources.files.CheckPathExistenceRemoteOperation

class RemoteFileUtils {
    companion object {

        fun getAvailableRemotePath(
            ownCloudClient: OwnCloudClient,
            remotePath: String,
            spaceWebDavUrl: String? = null,
            isUserLogged: Boolean,
        ): String {
            var checkExistsFile = existsFile(
                ownCloudClient = ownCloudClient,
                remotePath = remotePath,
                spaceWebDavUrl = spaceWebDavUrl,
                isUserLogged = isUserLogged,
            )
            if (!checkExistsFile) {
                return remotePath
            }
            val pos = remotePath.lastIndexOf(".")
            var suffix: String
            var extension = ""
            if (pos >= 0) {
                extension = remotePath.substring(pos + 1)
                remotePath.apply {
                    substring(0, pos)
                }
            }
            var count = 1
            do {
                suffix = " ($count)"
                checkExistsFile = if (pos >= 0) {
                    existsFile(
                        ownCloudClient = ownCloudClient,
                        remotePath = "${remotePath.substringBeforeLast('.', "")}$suffix.$extension",
                        spaceWebDavUrl = spaceWebDavUrl,
                        isUserLogged = isUserLogged,
                    )
                } else {
                    existsFile(
                        ownCloudClient = ownCloudClient,
                        remotePath = remotePath + suffix,
                        spaceWebDavUrl = spaceWebDavUrl,
                        isUserLogged = isUserLogged,
                    )
                }
                count++
            } while (checkExistsFile)
            return if (pos >= 0) {
                "${remotePath.substringBeforeLast('.', "")}$suffix.$extension"
            } else {
                remotePath + suffix
            }
        }

        private fun existsFile(
            ownCloudClient: OwnCloudClient,
            remotePath: String,
            spaceWebDavUrl: String?,
            isUserLogged: Boolean,
        ): Boolean {
            val existsOperation =
                CheckPathExistenceRemoteOperation(
                    remotePath = remotePath,
                    isUserLoggedIn = isUserLogged,
                    spaceWebDavUrl = spaceWebDavUrl,
                )
            return existsOperation.execute(ownCloudClient).isSuccess
        }
    }
}
