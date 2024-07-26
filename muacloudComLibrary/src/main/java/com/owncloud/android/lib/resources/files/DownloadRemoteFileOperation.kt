
package com.owncloud.android.lib.resources.files

import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.http.HttpConstants
import com.owncloud.android.lib.common.http.methods.nonwebdav.GetMethod
import com.owncloud.android.lib.common.http.methods.webdav.DavConstants
import com.owncloud.android.lib.common.http.methods.webdav.DavUtils
import com.owncloud.android.lib.common.http.methods.webdav.PropfindMethod
import com.owncloud.android.lib.common.network.OnDatatransferProgressListener
import com.owncloud.android.lib.common.network.WebdavUtils
import com.owncloud.android.lib.common.operations.OperationCancelledException
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import timber.log.Timber
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.concurrent.atomic.AtomicBoolean


class DownloadRemoteFileOperation(
    private val remotePath: String,
    localFolderPath: String,
    private val spaceWebDavUrl: String? = null,
) : RemoteOperation<Unit>() {

    private val cancellationRequested = AtomicBoolean(false)
    private val dataTransferListeners: MutableSet<OnDatatransferProgressListener> = HashSet()

    var modificationTimestamp: Long = 0
        private set

    var etag: String = ""
        private set

    override fun run(client: OwnCloudClient): RemoteOperationResult<Unit> {

        val tmpFile = File(tmpPath)

        val propfindMethod = PropfindMethod(
            URL(client.userFilesWebDavUri.toString()),
            DavConstants.DEPTH_1,
            DavUtils.allPropSet
        )
        val status = client.executeHttpMethod(propfindMethod)

        return try {
            tmpFile.parentFile?.mkdirs()
            downloadFile(client, tmpFile).also {
                Timber.i("Download of $remotePath to $tmpPath - HTTP status code: $status")
            }
        } catch (e: Exception) {
            RemoteOperationResult<Unit>(e).also { result ->
                Timber.e(e, "Download of $remotePath to $tmpPath: ${result.logMessage}")
            }
        }
    }

    @Throws(Exception::class)
    private fun downloadFile(client: OwnCloudClient, targetFile: File): RemoteOperationResult<Unit> {
        val result: RemoteOperationResult<Unit>
        var it: Iterator<OnDatatransferProgressListener>
        var fos: FileOutputStream? = null
        var bis: BufferedInputStream? = null
        var savedFile = false

        val webDavUri = spaceWebDavUrl ?: client.userFilesWebDavUri.toString()
        val getMethod = GetMethod(URL(webDavUri + WebdavUtils.encodePath(remotePath)))

        try {
            val status = client.executeHttpMethod(getMethod)

            if (isSuccess(status)) {
                targetFile.createNewFile()
                bis = BufferedInputStream(getMethod.getResponseBodyAsStream())
                fos = FileOutputStream(targetFile)
                var transferred: Long = 0
                val contentLength = getMethod.getResponseHeader(HttpConstants.CONTENT_LENGTH_HEADER)
                val totalToTransfer = if (!contentLength.isNullOrEmpty()) {
                    contentLength.toLong()
                } else {
                    -1L
                }
                val bytes = ByteArray(4096)
                var readResult: Int
                while (bis.read(bytes).also { readResult = it } != -1) {
                    synchronized(cancellationRequested) {
                        if (cancellationRequested.get()) {
                            getMethod.abort()
                            throw OperationCancelledException()
                        }
                    }
                    fos.write(bytes, 0, readResult)
                    transferred += readResult.toLong()
                    synchronized(dataTransferListeners) {
                        it = dataTransferListeners.iterator()
                        while (it.hasNext()) {
                            it.next()
                                .onTransferProgress(readResult.toLong(), transferred, totalToTransfer, targetFile.name)
                        }
                    }
                }

                if (totalToTransfer == -1L || transferred == totalToTransfer) {  // Check if the file is completed
                    savedFile = true
                    val modificationTime =
                        getMethod.getResponseHeaders()?.get("Last-Modified")
                            ?: getMethod.getResponseHeader("last-modified")

                    if (modificationTime != null) {
                        val modificationDate = WebdavUtils.parseResponseDate(modificationTime)
                        modificationTimestamp = modificationDate?.time ?: 0
                    } else {
                        Timber.e("Could not read modification time from response downloading %s", remotePath)
                    }
                    etag = WebdavUtils.getEtagFromResponse(getMethod)

                    etag = etag.replace("\"", "")
                    if (etag.isEmpty()) {
                        Timber.e("Could not read eTag from response downloading %s", remotePath)
                    }
                } else {
                    Timber.e("Content-Length not equal to transferred bytes.")
                    Timber.d("totalToTransfer = $totalToTransfer, transferred = $transferred")
                    client.exhaustResponse(getMethod.getResponseBodyAsStream())

                }

            } else if (status != HttpConstants.HTTP_FORBIDDEN && status != HttpConstants.HTTP_SERVICE_UNAVAILABLE) {
                client.exhaustResponse(getMethod.getResponseBodyAsStream())
            } // else, body read by RemoteOperationResult constructor

            result =
                if (isSuccess(status)) {
                    RemoteOperationResult(RemoteOperationResult.ResultCode.OK)
                } else {
                    RemoteOperationResult(getMethod)
                }
        } finally {
            fos?.close()
            bis?.close()
            if (!savedFile && targetFile.exists()) {
                targetFile.delete()
            }
        }
        return result
    }

    private fun isSuccess(status: Int) = status == HttpConstants.HTTP_OK

    private val tmpPath: String = localFolderPath + remotePath

    fun addDatatransferProgressListener(listener: OnDatatransferProgressListener) {
        synchronized(dataTransferListeners) { dataTransferListeners.add(listener) }
    }

    fun removeDatatransferProgressListener(listener: OnDatatransferProgressListener?) {
        synchronized(dataTransferListeners) { dataTransferListeners.remove(listener) }
    }

    fun cancel() {
        cancellationRequested.set(true) // atomic set; there is no need of synchronizing it
    }
}
