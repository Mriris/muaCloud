package com.owncloud.android.lib.resources.files

import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.http.HttpConstants
import com.owncloud.android.lib.common.http.methods.webdav.DavConstants
import com.owncloud.android.lib.common.http.methods.webdav.DavUtils
import com.owncloud.android.lib.common.http.methods.webdav.PropfindMethod
import com.owncloud.android.lib.common.http.methods.webdav.PutMethod
import com.owncloud.android.lib.common.network.FileRequestBody
import com.owncloud.android.lib.common.network.OnDatatransferProgressListener
import com.owncloud.android.lib.common.network.WebdavUtils
import com.owncloud.android.lib.common.operations.OperationCancelledException
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.operations.RemoteOperationResult.ResultCode
import com.owncloud.android.lib.common.utils.isOneOf
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import timber.log.Timber
import java.io.File
import java.net.URL
import java.util.concurrent.atomic.AtomicBoolean


open class UploadFileFromFileSystemOperation(
    val localPath: String,
    val remotePath: String,
    val mimeType: String,
    val lastModifiedTimestamp: String,
    val requiredEtag: String?,
    val spaceWebDavUrl: String? = null,
) : RemoteOperation<Unit>() {

    protected val cancellationRequested = AtomicBoolean(false)
    protected var putMethod: PutMethod? = null
    protected val dataTransferListener: MutableSet<OnDatatransferProgressListener> = HashSet()
    protected var fileRequestBody: FileRequestBody? = null

    var etag: String = ""

    override fun run(client: OwnCloudClient): RemoteOperationResult<Unit> {
        var result: RemoteOperationResult<Unit>
        try {
            val propfindMethod = PropfindMethod(
                URL(client.userFilesWebDavUri.toString()),
                DavConstants.DEPTH_1,
                DavUtils.allPropSet
            )
            val status = client.executeHttpMethod(propfindMethod)

            if (cancellationRequested.get()) {

                result = RemoteOperationResult<Unit>(OperationCancelledException())
                Timber.i("Upload of $localPath to $remotePath has been cancelled")
            } else {

                result = uploadFile(client)
                Timber.i("Upload of $localPath to $remotePath - HTTP status code: $status")
            }
        } catch (e: Exception) {
            if (putMethod?.isAborted == true) {
                result = RemoteOperationResult<Unit>(OperationCancelledException())
                Timber.e(result.exception, "Upload of $localPath to $remotePath has been aborted with this message: ${result.logMessage}")
            } else {
                result = RemoteOperationResult<Unit>(e)
                Timber.e(result.exception, "Upload of $localPath to $remotePath has failed with this message: ${result.logMessage}")
            }
        }
        return result
    }

    @Throws(Exception::class)
    protected open fun uploadFile(client: OwnCloudClient): RemoteOperationResult<Unit> {
        val fileToUpload = File(localPath)
        val mediaType: MediaType? = mimeType.toMediaTypeOrNull()

        fileRequestBody = FileRequestBody(fileToUpload, mediaType).also {
            synchronized(dataTransferListener) { it.addDatatransferProgressListeners(dataTransferListener) }
        }

        val baseStringUrl = spaceWebDavUrl ?: client.userFilesWebDavUri.toString()
        putMethod = PutMethod(URL(baseStringUrl + WebdavUtils.encodePath(remotePath)), fileRequestBody!!).apply {
            retryOnConnectionFailure = false
            if (!requiredEtag.isNullOrBlank()) {
                addRequestHeader(HttpConstants.IF_MATCH_HEADER, requiredEtag)
            }
            addRequestHeader(HttpConstants.OC_TOTAL_LENGTH_HEADER, fileToUpload.length().toString())
            addRequestHeader(HttpConstants.OC_X_OC_MTIME_HEADER, lastModifiedTimestamp)
        }

        val status = client.executeHttpMethod(putMethod)
        return if (isSuccess(status)) {
            etag = WebdavUtils.getEtagFromResponse(putMethod)

            etag = etag.replace("\"", "")
            if (etag.isEmpty()) {
                Timber.e("Could not read eTag from response uploading %s", localPath)
            } else {
                Timber.d("File uploaded successfully. New etag for file ${fileToUpload.name} is $etag")
            }
            RemoteOperationResult<Unit>(ResultCode.OK).apply { data = Unit }
        } else { // synchronization failed
            RemoteOperationResult<Unit>(putMethod)
        }
    }

    fun addDataTransferProgressListener(listener: OnDatatransferProgressListener) {
        synchronized(dataTransferListener) { dataTransferListener.add(listener) }
        fileRequestBody?.addDatatransferProgressListener(listener)
    }

    fun removeDataTransferProgressListener(listener: OnDatatransferProgressListener) {
        synchronized(dataTransferListener) { dataTransferListener.remove(listener) }
        fileRequestBody?.removeDatatransferProgressListener(listener)
    }

    fun cancel() {
        synchronized(cancellationRequested) {
            cancellationRequested.set(true)
            putMethod?.abort()
        }
    }

    fun isSuccess(status: Int): Boolean {
        return status.isOneOf(HttpConstants.HTTP_OK, HttpConstants.HTTP_CREATED, HttpConstants.HTTP_NO_CONTENT)
    }
}
