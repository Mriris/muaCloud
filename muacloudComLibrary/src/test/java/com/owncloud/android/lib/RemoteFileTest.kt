package com.owncloud.android.lib

import android.os.Build
import com.owncloud.android.lib.resources.files.RemoteFile
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O], manifest = Config.NONE)
class RemoteFileTest {

    @Test
    fun getRemotePathFromUrl_legacyWebDav() {
        val httpUrlToTest = "https://server.url/remote.php/dav/files/username/Documents/text.txt".toHttpUrl()
        val expectedRemotePath = "/Documents/text.txt"

        val actualRemotePath = RemoteFile.Companion.getRemotePathFromUrl(httpUrlToTest, "username")
        assertEquals(expectedRemotePath, actualRemotePath)
    }

    @Test
    fun getRemotePathFromUrl_spacesWebDav() {
        val spaceWebDavUrl = "https://server.url/dav/spaces/8871f4f3-fc6f-4a66-8bed-62f175f76f38$05bca744-d89f-4e9c-a990-25a0d7f03fe9"

        val httpUrlToTest =
            "https://server.url/dav/spaces/8871f4f3-fc6f-4a66-8bed-62f175f76f38$05bca744-d89f-4e9c-a990-25a0d7f03fe9/Documents/text.txt".toHttpUrl()
        val expectedRemotePath = "/Documents/text.txt"

        val actualRemotePath = RemoteFile.Companion.getRemotePathFromUrl(httpUrlToTest, "username", spaceWebDavUrl)
        assertEquals(expectedRemotePath, actualRemotePath)
    }
}
