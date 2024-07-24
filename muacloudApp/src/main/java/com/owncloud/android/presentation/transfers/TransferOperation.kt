
package com.owncloud.android.presentation.transfers

sealed interface TransferOperation {
    data class Download(val downloadPath: String) : TransferOperation
    data class Upload(val fileName: String) : TransferOperation
}
