package com.owncloud.android.lib.common.network

import com.owncloud.android.lib.resources.files.chunks.ChunkedUploadFromFileSystemOperation.Companion.CHUNK_SIZE
import okhttp3.MediaType
import okio.BufferedSink
import timber.log.Timber
import java.io.File
import java.nio.ByteBuffer
import java.nio.channels.FileChannel


class ChunkFromFileRequestBody(
    file: File,
    contentType: MediaType?,
    private val channel: FileChannel,
    private val chunkSize: Long = CHUNK_SIZE
) : FileRequestBody(file, contentType) {

    private var offset: Long = 0
    private var alreadyTransferred: Long = 0
    private val buffer = ByteBuffer.allocate(4_096)

    init {
        require(chunkSize > 0) { "Chunk size must be greater than zero" }
    }

    override fun contentLength(): Long {
        return chunkSize.coerceAtMost(channel.size() - channel.position())
    }

    override fun writeTo(sink: BufferedSink) {
        var readCount: Int
        var iterator: Iterator<OnDatatransferProgressListener>
        try {
            channel.position(offset)

            val maxCount = (offset + chunkSize).coerceAtMost(channel.size())
            while (channel.position() < maxCount) {
                readCount = channel.read(buffer)
                val bytesToWriteInBuffer = readCount.toLong().coerceAtMost(file.length() - alreadyTransferred).toInt()
                sink.buffer.write(buffer.array(), 0, bytesToWriteInBuffer)
                sink.flush()
                buffer.clear()

                if (alreadyTransferred < maxCount) {  // condition to avoid accumulate progress for repeated chunks
                    alreadyTransferred += readCount.toLong()
                }

                synchronized(dataTransferListeners) {
                    iterator = dataTransferListeners.iterator()
                    while (iterator.hasNext()) {
                        iterator.next().onTransferProgress(readCount.toLong(), alreadyTransferred, file.length(), file.absolutePath)
                    }
                }
            }
        } catch (exception: Exception) {
            Timber.e(exception, "Transferred " + alreadyTransferred + " bytes from a total of " + file.length())
        }
    }

    fun setOffset(newOffset: Long) {
        offset = newOffset
    }

}
