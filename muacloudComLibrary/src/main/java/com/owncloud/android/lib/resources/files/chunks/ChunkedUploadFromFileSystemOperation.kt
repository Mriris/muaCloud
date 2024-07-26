package com.owncloud.android.lib.resources.files.chunks

import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.http.methods.webdav.PutMethod
import com.owncloud.android.lib.common.network.ChunkFromFileRequestBody
import com.owncloud.android.lib.common.operations.OperationCancelledException
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.operations.RemoteOperationResult.ResultCode
import com.owncloud.android.lib.resources.files.FileUtils.MODE_READ_ONLY
import com.owncloud.android.lib.resources.files.UploadFileFromFileSystemOperation
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import timber.log.Timber
import java.io.File
import java.io.RandomAccessFile
import java.net.URL
import java.nio.channels.FileChannel
import java.util.concurrent.TimeUnit
import kotlin.math.ceil


class ChunkedUploadFromFileSystemOperation(
    private val transferId: String,
    localPath: String,
    remotePath: String,
    mimeType: String,
    lastModifiedTimestamp: String,
    requiredEtag: String?,
) : UploadFileFromFileSystemOperation(
    localPath = localPath,
    remotePath = remotePath,
    mimeType = mimeType,
    lastModifiedTimestamp = lastModifiedTimestamp,
    requiredEtag = requiredEtag
) {

    @Throws(Exception::class)
    override fun uploadFile(client: OwnCloudClient): RemoteOperationResult<Unit> {
        lateinit var result: RemoteOperationResult<Unit>

        val fileToUpload = File(localPath)
        val mediaType: MediaType? = mimeType.toMediaTypeOrNull()
        val raf = RandomAccessFile(fileToUpload, MODE_READ_ONLY)
        val channel: FileChannel = raf.channel

        val fileRequestBody = ChunkFromFileRequestBody(fileToUpload, mediaType, channel).also {
            synchronized(dataTransferListener) { it.addDatatransferProgressListeners(dataTransferListener) }
        }

        val uriPrefix = client.uploadsWebDavUri.toString() + File.separator + transferId
        val totalLength = fileToUpload.length()
        val chunkCount = ceil(totalLength.toDouble() / CHUNK_SIZE).toLong()
        var offset: Long = 0

        for (chunkIndex in 0..chunkCount) {
            fileRequestBody.setOffset(offset)

            if (cancellationRequested.get()) {
                result = RemoteOperationResult<Unit>(OperationCancelledException())
                break
            } else {
                putMethod = PutMethod(URL(uriPrefix + File.separator + chunkIndex), fileRequestBody).apply {
                    if (chunkIndex == chunkCount - 1) {



                        setReadTimeout(LAST_CHUNK_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
                    }
                }

                val status = client.executeHttpMethod(putMethod)

                Timber.d("Upload of $localPath to $remotePath, chunk index $chunkIndex, count $chunkCount, HTTP result status $status")

                if (isSuccess(status)) {
                    result = RemoteOperationResult<Unit>(ResultCode.OK)
                } else {
                    result = RemoteOperationResult<Unit>(putMethod)
                    break
                }
            }
            offset += CHUNK_SIZE
        }
        channel.close()
        raf.close()
        return result
    }

    companion object {
        const val CHUNK_SIZE = 10_240_000L // 10 MB
        private const val LAST_CHUNK_TIMEOUT = 900_000 // 15 mins.
    }
}
