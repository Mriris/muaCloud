

package com.owncloud.android.extensions

import androidx.work.WorkInfo
import com.owncloud.android.domain.extensions.isOneOf
import com.owncloud.android.workers.DownloadFileWorker
import com.owncloud.android.workers.UploadFileFromContentUriWorker
import com.owncloud.android.workers.UploadFileFromFileSystemWorker

fun WorkInfo.isUpload() =
    tags.any { it.isOneOf(UploadFileFromContentUriWorker::class.java.name, UploadFileFromFileSystemWorker::class.java.name) }

fun WorkInfo.isDownload() =
    tags.any { it.isOneOf(DownloadFileWorker::class.java.name) }
