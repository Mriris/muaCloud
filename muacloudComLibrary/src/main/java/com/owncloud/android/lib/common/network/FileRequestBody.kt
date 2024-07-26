package com.owncloud.android.lib.common.network

import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import okio.Source
import okio.source
import timber.log.Timber
import java.io.File
import java.util.HashSet


open class FileRequestBody(
    val file: File,
    private val contentType: MediaType?,
) : RequestBody(), ProgressiveDataTransferer {

    val dataTransferListeners: MutableSet<OnDatatransferProgressListener> = HashSet()

    override fun isOneShot(): Boolean = true

    override fun contentType(): MediaType? = contentType

    override fun contentLength(): Long = file.length()

    override fun writeTo(sink: BufferedSink) {
        val source: Source
        var it: Iterator<OnDatatransferProgressListener>
        try {
            source = file.source()
            var transferred: Long = 0
            var read: Long
            while (source.read(sink.buffer, BYTES_TO_READ).also { read = it } != -1L) {
                transferred += read
                sink.flush()
                synchronized(dataTransferListeners) {
                    it = dataTransferListeners.iterator()
                    while (it.hasNext()) {
                        it.next().onTransferProgress(read, transferred, file.length(), file.absolutePath)
                    }
                }
            }
            Timber.d("File with name ${file.name} and size ${file.length()} written in request body")
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override fun addDatatransferProgressListener(listener: OnDatatransferProgressListener) {
        synchronized(dataTransferListeners) {
            dataTransferListeners.add(listener)
        }
    }

    override fun addDatatransferProgressListeners(listeners: Collection<OnDatatransferProgressListener>) {
        synchronized(dataTransferListeners) {
            dataTransferListeners.addAll(listeners)
        }
    }

    override fun removeDatatransferProgressListener(listener: OnDatatransferProgressListener) {
        synchronized(dataTransferListeners) {
            dataTransferListeners.remove(listener)
        }
    }

    companion object {
        private const val BYTES_TO_READ = 4_096L
    }
}
