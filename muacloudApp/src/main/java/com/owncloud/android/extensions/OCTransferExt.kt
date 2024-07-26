

package com.owncloud.android.extensions

import android.content.Context
import android.net.Uri
import androidx.annotation.StringRes
import androidx.documentfile.provider.DocumentFile
import com.owncloud.android.R
import com.owncloud.android.domain.transfers.model.OCTransfer
import com.owncloud.android.domain.transfers.model.TransferResult
import com.owncloud.android.domain.transfers.model.TransferStatus

@StringRes
fun OCTransfer.statusToStringRes(): Int {
     return when (status) {
         TransferStatus.TRANSFER_IN_PROGRESS -> R.string.uploader_upload_in_progress_ticker
         TransferStatus.TRANSFER_SUCCEEDED -> R.string.uploads_view_upload_status_succeeded
         TransferStatus.TRANSFER_QUEUED -> R.string.uploads_view_upload_status_queued
         TransferStatus.TRANSFER_FAILED -> when (lastResult) {
             TransferResult.CREDENTIAL_ERROR -> R.string.uploads_view_upload_status_failed_credentials_error
             TransferResult.FOLDER_ERROR -> R.string.uploads_view_upload_status_failed_folder_error
             TransferResult.FILE_NOT_FOUND -> R.string.uploads_view_upload_status_failed_localfile_error
             TransferResult.FILE_ERROR -> R.string.uploads_view_upload_status_failed_file_error
             TransferResult.PRIVILEGES_ERROR -> R.string.uploads_view_upload_status_failed_permission_error
             TransferResult.NETWORK_CONNECTION -> R.string.uploads_view_upload_status_failed_connection_error
             TransferResult.DELAYED_FOR_WIFI -> R.string.uploads_view_upload_status_waiting_for_wifi
             TransferResult.CONFLICT_ERROR -> R.string.uploads_view_upload_status_conflict
             TransferResult.SERVICE_INTERRUPTED -> R.string.uploads_view_upload_status_service_interrupted
             TransferResult.SERVICE_UNAVAILABLE -> R.string.service_unavailable
             TransferResult.QUOTA_EXCEEDED -> R.string.failed_upload_quota_exceeded_text
             TransferResult.SSL_RECOVERABLE_PEER_UNVERIFIED -> R.string.ssl_certificate_not_trusted
             TransferResult.UNKNOWN -> R.string.uploads_view_upload_status_unknown_fail

             TransferResult.CANCELLED -> R.string.uploads_view_upload_status_cancelled

             TransferResult.UPLOADED -> R.string.uploads_view_upload_status_succeeded

             TransferResult.SPECIFIC_FORBIDDEN -> R.string.uploads_view_upload_status_failed_permission_error

             TransferResult.SPECIFIC_SERVICE_UNAVAILABLE -> R.string.service_unavailable

             TransferResult.SPECIFIC_UNSUPPORTED_MEDIA_TYPE -> R.string.uploads_view_unsupported_media_type

             null -> R.string.uploads_view_upload_status_unknown_fail
         }
    }
}

fun OCTransfer.isContentUri(context: Context): Boolean {
    return DocumentFile.isDocumentUri(context, Uri.parse(localPath))
}
